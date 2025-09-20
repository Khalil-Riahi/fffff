package com.projet.freelencetinder.servcie;

import static com.projet.freelencetinder.servcie.FeedbackEvents.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.server.ResponseStatusException;

import com.projet.freelencetinder.dto.FeedbackDTOs.*;
import com.projet.freelencetinder.models.*;
import com.projet.freelencetinder.repository.*;

@Service
public class FeedbackService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackService.class);

    private final FeedbackRepository feedbackRepo;
    private final FeedbackCriterionScoreRepository scoreRepo;
    private final FeedbackWindowRepository windowRepo;
    private final FeedbackAggregateRepository aggRepo;
    private final FeedbackRulesService rules;
    private final AntiAbuseLightService antiAbuse;
    private final ApplicationEventPublisher publisher;
    private final TaskScheduler scheduler;
    private final com.projet.freelencetinder.repository.MissionRepository missionRepo;

    public FeedbackService(FeedbackRepository feedbackRepo,
                           FeedbackCriterionScoreRepository scoreRepo,
                           FeedbackWindowRepository windowRepo,
                           FeedbackAggregateRepository aggRepo,
                           FeedbackRulesService rules,
                           AntiAbuseLightService antiAbuse,
                           ApplicationEventPublisher publisher,
                           TaskScheduler scheduler,
                           com.projet.freelencetinder.repository.MissionRepository missionRepo) {
        this.feedbackRepo = feedbackRepo;
        this.scoreRepo = scoreRepo;
        this.windowRepo = windowRepo;
        this.aggRepo = aggRepo;
        this.rules = rules;
        this.antiAbuse = antiAbuse;
        this.publisher = publisher;
        this.scheduler = scheduler;
        this.missionRepo = missionRepo;
    }

    @Transactional(readOnly = true)
    public FeedbackEligibilityDTO getEligibility(Long missionId, Long requesterId, Long clientId, Long freelancerId) {
        FeedbackEligibilityDTO dto = new FeedbackEligibilityDTO();
        boolean participant = requesterId != null && (requesterId.equals(clientId) || requesterId.equals(freelancerId));
        dto.eligible = participant;
        dto.reason = participant ? "OK" : "Accès refusé";
        windowRepo.findByMissionId(missionId).ifPresent(w -> dto.deadline = w.getExpiresAt());
        return dto;
    }

    @Transactional(readOnly = true)
    public FeedbackWindowDTO getWindow(Long missionId) {
        FeedbackWindow w = windowRepo.findByMissionId(missionId).orElse(null);
        FeedbackWindowDTO dto = new FeedbackWindowDTO();
        if (w != null) {
            dto.openedAt = w.getOpenedAt();
            dto.expiresAt = w.getExpiresAt();
            dto.clientSubmitted = w.isClientSubmitted();
            dto.freelancerSubmitted = w.isFreelancerSubmitted();
            dto.doubleBlind = !(w.isClientSubmitted() && w.isFreelancerSubmitted());
            dto.autoPublishedAt = w.getAutoPublishedAt();
        }
        return dto;
    }

    @Transactional
    public void openWindowOnMissionClosed(Long missionId, Long clientId, Long freelancerId) {
        if (windowRepo.findByMissionId(missionId).isPresent()) return;
        FeedbackWindow w = new FeedbackWindow();
        w.setMissionId(missionId);
        w.setClientId(clientId);
        w.setFreelancerId(freelancerId);
        w.setOpenedAt(Instant.now());
        w.setExpiresAt(Instant.now().plus(Duration.ofDays(14)));
        windowRepo.save(w);
        scheduler.schedule(() -> publisher.publishEvent(new FeedbackAutoPublishDueEvent(missionId, w.getExpiresAt())), java.util.Date.from(w.getExpiresAt()));
    }

    @Transactional
    public FeedbackResponseDTO submit(FeedbackCreateRequestDTO req, Long issuerId, String ipHash, String uaHash) {
        // === Contexte logging ===
        try {
            MDC.put("missionId", String.valueOf(req.missionId));
            MDC.put("issuerId", String.valueOf(issuerId));
            MDC.put("role", String.valueOf(req.role));
            MDC.put("targetId", String.valueOf(req.targetId));
            if (req.idempotencyKey != null) MDC.put("idemKey", req.idempotencyKey);

            log.info("Feedback submit INIT");

            // 1) Mission + participants
            var mission = missionRepo.findById(req.missionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "MISSION_NOT_FOUND"));

            Long clientId = mission.getClient() != null ? mission.getClient().getId() : null;
            Long freelancerId = mission.getFreelanceSelectionne() != null ? mission.getFreelanceSelectionne().getId() : null;

            if (clientId == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "MISSION_WITHOUT_CLIENT");
            }

            boolean issuerIsClient = issuerId.equals(clientId);
            boolean issuerIsFreelancer = (freelancerId != null && issuerId.equals(freelancerId));

            if (!(issuerIsClient || issuerIsFreelancer)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ACCESS_DENIED");
            }

            // (Optionnel) Vérifier que targetId pointe l'autre partie (sécurité / données propres)
            if (req.role == FeedbackRole.CLIENT_TO_FREELANCER) {
                if (freelancerId == null) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "FREELANCER_NOT_ASSIGNED");
                }
                if (!freelancerId.equals(req.targetId)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "INVALID_TARGET");
                }
            } else { // FREELANCER_TO_CLIENT
                if (!clientId.equals(req.targetId)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "INVALID_TARGET");
                }
            }

            // 2) Charger ou créer la fenêtre
            FeedbackWindow window = windowRepo.findByMissionId(req.missionId).orElse(null);
            if (window == null) {
                boolean eligible =
                       mission.getStatut() == Mission.Statut.PRET_A_CLOTURER
                    || mission.getStatut() == Mission.Statut.EN_ATTENTE_VALIDATION
                    || mission.getStatut() == Mission.Statut.TERMINEE;

                if (!eligible) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "WINDOW_NOT_OPEN");
                }

                window = new FeedbackWindow();
                window.setMissionId(req.missionId);
                window.setClientId(clientId);
                window.setFreelancerId(freelancerId);
                window.setOpenedAt(Instant.now());
                window.setExpiresAt(Instant.now().plus(Duration.ofDays(14)));
                windowRepo.save(window);

                log.info("Feedback window CREATED for mission {}", req.missionId);
            }

            // 3) Fenêtre non expirée ?
            if (Instant.now().isAfter(window.getExpiresAt())) {
                throw new ResponseStatusException(HttpStatus.GONE, "WINDOW_EXPIRED");
            }

            // 4) Idempotence / unicité logique
            if (req.idempotencyKey != null && !req.idempotencyKey.isBlank()) {
                boolean dup = feedbackRepo.existsByMissionIdAndIssuerIdAndTargetIdAndRoleAndIdempotencyKey(
                    req.missionId, issuerId, req.targetId, req.role, req.idempotencyKey);
                if (dup) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "REQUEST_ALREADY_PROCESSED");
                }
            }

            if (feedbackRepo.existsByMissionIdAndIssuerIdAndTargetIdAndRoleAndStatusNot(
                    req.missionId, issuerId, req.targetId, req.role, FeedbackStatus.REMOVED)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "FEEDBACK_ALREADY_EXISTS");
            }

            // 5) Construire + persister le feedback (reprend ta logique existante)
            Map<CriterionType, BigDecimal> weights = rules.weightsFor(req.role);

            Feedback f = new Feedback();
            f.setMissionId(req.missionId);
            f.setIssuerId(issuerId);
            f.setTargetId(req.targetId);
            f.setRole(req.role);
            f.setStatus(FeedbackStatus.SUBMITTED);
            f.setComment(req.comment);
            f.setIpHash(ipHash);
            f.setUserAgentHash(uaHash);
            f.setSubmittedAt(Instant.now());
            if (req.idempotencyKey != null) f.setIdempotencyKey(req.idempotencyKey);

            BigDecimal sum = BigDecimal.ZERO;
            BigDecimal wsum = BigDecimal.ZERO;
            if (req.scores != null) {
                for (ScoreItem si : req.scores) {
                    // Validation/clamp par sécurité
                    if (si.score == null || si.score < 1 || si.score > 5) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Score invalide: doit être entre 1 et 5");
                    }
                    final int s = Math.max(1, Math.min(5, si.score));
                    
                    BigDecimal w = weights.getOrDefault(si.criterion, BigDecimal.ZERO);
                    if (w.signum() > 0) {
                        sum = sum.add(w.multiply(BigDecimal.valueOf(s)));
                        wsum = wsum.add(w);
                    }
                    FeedbackCriterionScore sc = new FeedbackCriterionScore();
                    sc.setCriterion(si.criterion);
                    sc.setScore(s);
                    sc.setWeight(w);
                    sc.setWeightSource(com.projet.freelencetinder.models.WeightSource.STATIC);
                    f.addScore(sc);
                }
            }
            if (wsum.signum() > 0) {
                BigDecimal avg = sum.divide(wsum, 1, RoundingMode.HALF_UP);
                f.setOverallRating(avg);
            }
            feedbackRepo.save(f);
            log.info("Feedback persisted id={}", f.getId());

            // 6) Marquer la fenêtre (avec verrou pessimiste pour éviter les races)
            FeedbackWindow wLock = windowRepo.findByMissionIdForUpdate(req.missionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "WINDOW_NOT_FOUND_AFTER_SAVE"));
            boolean fromClient = issuerId.equals(wLock.getClientId());
            if (fromClient) wLock.setClientSubmitted(true); else wLock.setFreelancerSubmitted(true);
            windowRepo.save(wLock);
            log.info("Window flags updated: clientSubmitted={}, freelancerSubmitted={}",
                     wLock.isClientSubmitted(), wLock.isFreelancerSubmitted());

            // 7) Événements existants / anti-abuse (inchangé)
            publisher.publishEvent(new FeedbackSubmittedEvent(req.missionId, f.getId()));
            publisher.publishEvent(new RealTimeNotificationEvent("feedback.submitted",
                    java.util.Map.of("missionId", req.missionId, "id", f.getId())));

            int risk = antiAbuse.riskScore(f);
            if (risk >= 80) {
                f.setStatus(FeedbackStatus.UNDER_REVIEW);
                feedbackRepo.save(f);
                antiAbuse.scheduleAutoRelease(f.getId(), Instant.now());
                log.info("Feedback set UNDER_REVIEW, risk={}", risk);
            }

            if (wLock.isClientSubmitted() && wLock.isFreelancerSubmitted()) {
                publisher.publishEvent(new FeedbackPairReadyEvent(req.missionId));
                log.info("Feedback pair ready, mission={}", req.missionId);
            }

            FeedbackResponseDTO out = new FeedbackResponseDTO();
            out.id = f.getId();
            out.missionId = f.getMissionId();
            out.issuerId = f.getIssuerId();
            out.targetId = f.getTargetId();
            out.role = f.getRole();
            out.overallRating = f.getOverallRating();
            out.comment = f.getComment();
            out.submittedAt = f.getSubmittedAt();
            out.publishedAt = f.getPublishedAt();

            log.info("Feedback submit DONE");
            return out;

        } catch (ResponseStatusException ex) {
            log.warn("Feedback submit FAILED: status={}, reason={}", ex.getStatusCode(), ex.getReason());
            throw ex;
        } catch (Exception e) {
            log.error("Feedback submit ERROR", e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @Transactional
    public FeedbackResponseDTO updateFeedback(Long feedbackId, FeedbackUpdateRequestDTO req, Long currentUserId, String ipHash, String uaHash) {
        // 1. Charger le feedback
        Feedback feedback = feedbackRepo.findById(feedbackId)
            .orElseThrow(() -> new IllegalArgumentException("Feedback introuvable")); // 404

        // 2. Vérifier que l'utilisateur est l'auteur
        if (!feedback.getIssuerId().equals(currentUserId)) {
            throw new IllegalStateException("Accès refusé"); // 403
        }

        // 3. Vérifier le statut (seul SUBMITTED peut être modifié)
        if (feedback.getStatus() == FeedbackStatus.PUBLISHED) {
            throw new IllegalStateException("Feedback déjà publié, modification impossible"); // 409
        }
        if (feedback.getStatus() == FeedbackStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Feedback en cours de modération, modification bloquée"); // 409
        }

        // 4. Vérifier que la fenêtre n'est pas expirée
        FeedbackWindow window = windowRepo.findByMissionId(feedback.getMissionId())
            .orElseThrow(() -> new IllegalStateException("Fenêtre de feedback introuvable"));
        
        if (Instant.now().isAfter(window.getExpiresAt())) {
            throw new IllegalStateException("Fenêtre de feedback expirée, modification impossible"); // 410
        }

        // 5. Vérifier idempotency si fourni
        if (req.idempotencyKey != null && !req.idempotencyKey.isBlank()) {
            if (req.idempotencyKey.equals(feedback.getIdempotencyKey())) {
                // Même idempotency key, retourner le feedback existant
                return mapToResponseDTO(feedback);
            }
        }

        // 6. Mettre à jour le commentaire si fourni
        if (req.comment != null) {
            if (req.comment.length() < 30 || req.comment.length() > 800) {
                throw new IllegalArgumentException("Commentaire doit faire entre 30 et 800 caractères");
            }
            feedback.setComment(req.comment);
        }

        // 7. Mettre à jour les scores si fournis
        if (req.scores != null && !req.scores.isEmpty()) {
            log.info("Update Feedback - Payload reçu: scores={}", req.scores);
            
            // Supprimer les anciens scores
            feedback.clearScores();
            
            // Recalculer avec les nouveaux scores
            Map<CriterionType, BigDecimal> weights = rules.weightsFor(feedback.getRole());
            BigDecimal sum = BigDecimal.ZERO;
            BigDecimal weightSum = BigDecimal.ZERO;
            
            for (ScoreItem si : req.scores) {
                // Validation/clamp par sécurité
                if (si.score == null || si.score < 1 || si.score > 5) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Score invalide: doit être entre 1 et 5");
                }
                final int s = Math.max(1, Math.min(5, si.score));
                
                BigDecimal weight = weights.getOrDefault(si.criterion, BigDecimal.ZERO);
                if (weight.signum() > 0) {
                    sum = sum.add(weight.multiply(BigDecimal.valueOf(s)));
                    weightSum = weightSum.add(weight);
                }
                
                FeedbackCriterionScore score = new FeedbackCriterionScore();
                score.setCriterion(si.criterion);
                score.setScore(s);
                score.setWeight(weight);
                score.setWeightSource(WeightSource.STATIC);
                feedback.addScore(score);
            }
            
            // Recalculer la note globale
            if (weightSum.signum() > 0) {
                BigDecimal avg = sum.divide(weightSum, 1, RoundingMode.HALF_UP);
                feedback.setOverallRating(avg);
            }
        }

        // 8. Mettre à jour l'idempotency key si fourni
        if (req.idempotencyKey != null) {
            feedback.setIdempotencyKey(req.idempotencyKey);
        }

        // 9. Mettre à jour les hashs IP/UA
        feedback.setIpHash(ipHash);
        feedback.setUserAgentHash(uaHash);

        // 10. Sauvegarder
        feedbackRepo.save(feedback);

        // 11. Évaluer à nouveau le risque anti-abus
        int riskScore = antiAbuse.riskScore(feedback);
        if (riskScore >= 80 && feedback.getStatus() != FeedbackStatus.UNDER_REVIEW) {
            feedback.setStatus(FeedbackStatus.UNDER_REVIEW);
            feedbackRepo.save(feedback);
            antiAbuse.scheduleAutoRelease(feedback.getId(), Instant.now());
        }

        // 12. Diffuser événement temps réel
        publisher.publishEvent(new RealTimeNotificationEvent("feedback.updated", 
            java.util.Map.of("missionId", feedback.getMissionId(), "id", feedback.getId())));

        return mapToResponseDTO(feedback);
    }

    @Transactional(readOnly = true)
    public FeedbackResponseDTO getOwnFeedback(Long feedbackId, Long currentUserId) {
        // 1. Charger le feedback
        Feedback feedback = feedbackRepo.findById(feedbackId)
            .orElseThrow(() -> new IllegalArgumentException("Feedback introuvable")); // 404

        // 2. Vérifier que l'utilisateur est l'auteur
        if (!feedback.getIssuerId().equals(currentUserId)) {
            throw new IllegalStateException("Accès refusé"); // 403
        }

        return mapToResponseDTO(feedback);
    }

    @Transactional(readOnly = true)
    public FeedbackResponseDTO getOwnFeedbackByMission(Long missionId, Long currentUserId) {
        // 1. Charger le feedback par mission et utilisateur
        Feedback feedback = feedbackRepo.findByMissionIdAndIssuerId(missionId, currentUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FEEDBACK_NOT_FOUND"));

        // 2. Vérifier que l'utilisateur est bien l'auteur (double sécurité)
        if (!feedback.getIssuerId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }

        return mapToResponseDTO(feedback);
    }

    @Transactional
    public void deleteFeedback(Long feedbackId, Long currentUserId) {
        // 1. Charger le feedback
        Feedback feedback = feedbackRepo.findById(feedbackId)
            .orElseThrow(() -> new IllegalArgumentException("Feedback introuvable")); // 404

        // 2. Vérifier que l'utilisateur est l'auteur
        if (!feedback.getIssuerId().equals(currentUserId)) {
            throw new IllegalStateException("Accès refusé"); // 403
        }

        // 3. Vérifier que le feedback peut être supprimé (seulement avant publication)
        if (feedback.getStatus() == FeedbackStatus.PUBLISHED) {
            throw new IllegalStateException("Feedback déjà publié, suppression impossible"); // 409
        }

        // 4. Vérifier que la fenêtre n'est pas expirée
        FeedbackWindow window = windowRepo.findByMissionId(feedback.getMissionId())
            .orElseThrow(() -> new IllegalStateException("Fenêtre de feedback introuvable"));
        
        if (Instant.now().isAfter(window.getExpiresAt())) {
            throw new IllegalStateException("Fenêtre de feedback expirée, suppression impossible"); // 410
        }

        // 5. Mettre à jour la fenêtre (décocher le flag de soumission)
        FeedbackWindow wLock = windowRepo.findByMissionIdForUpdate(feedback.getMissionId())
            .orElseThrow(() -> new IllegalStateException("Fenêtre de feedback introuvable"));
        
        boolean fromClient = currentUserId.equals(wLock.getClientId());
        if (fromClient) {
            wLock.setClientSubmitted(false);
        } else {
            wLock.setFreelancerSubmitted(false);
        }
        windowRepo.save(wLock);

        // 6. Supprimer le feedback
        feedbackRepo.delete(feedback);

        // 7. Diffuser événement temps réel
        publisher.publishEvent(new RealTimeNotificationEvent("feedback.deleted", 
            java.util.Map.of("missionId", feedback.getMissionId(), "id", feedbackId)));

        log.info("Feedback deleted: id={}, issuerId={}", feedbackId, currentUserId);
    }

    private FeedbackResponseDTO mapToResponseDTO(Feedback f) {
        FeedbackResponseDTO dto = new FeedbackResponseDTO();
        dto.id = f.getId();
        dto.missionId = f.getMissionId();
        dto.issuerId = f.getIssuerId();
        dto.targetId = f.getTargetId();
        dto.role = f.getRole();
        dto.overallRating = f.getOverallRating();
        dto.comment = f.getComment();
        dto.submittedAt = f.getSubmittedAt();
        dto.publishedAt = f.getPublishedAt();
        return dto;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPairReady(FeedbackPairReadyEvent evt) {
        publishPair(evt.missionId());
    }

    @Transactional
    protected void publishPair(Long missionId) {
        Page<Feedback> page = feedbackRepo.findByMissionId(missionId, PageRequest.of(0, 10));
        List<Long> published = new ArrayList<>();
        for (Feedback f : page) {
            if (f.getStatus() == FeedbackStatus.SUBMITTED) {
                f.setStatus(FeedbackStatus.PUBLISHED);
                f.setPublishedAt(Instant.now());
                feedbackRepo.save(f);
                recomputeAggregates(f.getTargetId(), audienceFor(f.getRole()));
                published.add(f.getId());
            }
        }
        if (!published.isEmpty()) {
            publisher.publishEvent(new FeedbackPublishedEvent(missionId, published));
            // Notifier en temps réel
            publisher.publishEvent(new RealTimeNotificationEvent("feedback.published", java.util.Map.of("missionId", missionId, "ids", published)));
        }
    }

    private Audience audienceFor(FeedbackRole role) {
        return role == FeedbackRole.CLIENT_TO_FREELANCER ? Audience.FREELANCE : Audience.CLIENT;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAutoPublishDue(FeedbackAutoPublishDueEvent evt) {
        Page<Feedback> page = feedbackRepo.findByMissionId(evt.missionId(), PageRequest.of(0, 10));
        List<Long> published = new ArrayList<>();
        for (Feedback f : page) {
            if (f.getStatus() == FeedbackStatus.SUBMITTED) {
                f.setStatus(FeedbackStatus.PUBLISHED);
                f.setPublishedAt(Instant.now());
                f.setVisibilityReason("auto-published J+14");
                feedbackRepo.save(f);
                recomputeAggregates(f.getTargetId(), audienceFor(f.getRole()));
                published.add(f.getId());
            }
        }
        if (!published.isEmpty()) {
            publisher.publishEvent(new FeedbackPublishedEvent(evt.missionId(), published));
            publisher.publishEvent(new RealTimeNotificationEvent("feedback.published", java.util.Map.of("missionId", evt.missionId(), "ids", published)));
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUnderReviewRelease(FeedbackUnderReviewAutoReleaseEvent evt) {
        Feedback f = feedbackRepo.findById(evt.feedbackId()).orElse(null);
        if (f == null) return;
        if (f.getStatus() == FeedbackStatus.UNDER_REVIEW) {
            f.setStatus(FeedbackStatus.SUBMITTED);
            feedbackRepo.save(f);
        }
    }

    @Transactional(readOnly = true)
    public Page<FeedbackResponseDTO> listPublic(FeedbackListQueryDTO q) {
        Pageable pageable = PageRequest.of(q.page, q.size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<Feedback> page = feedbackRepo.findPublishedByTarget(q.targetId, q.periodStart, q.periodEnd, pageable);
        return page.map(f -> {
            FeedbackResponseDTO dto = new FeedbackResponseDTO();
            dto.id = f.getId();
            dto.missionId = f.getMissionId();
            dto.issuerId = f.getIssuerId();
            dto.targetId = f.getTargetId();
            dto.role = f.getRole();
            dto.overallRating = f.getOverallRating();
            dto.comment = f.getComment();
            dto.submittedAt = f.getSubmittedAt();
            dto.publishedAt = f.getPublishedAt();
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public FeedbackSummaryDTO summary(Long targetId, Audience audience, int windowDays) {
        // Ensure aggregate exists and is fresh enough by recomputing
        computeForWindow(targetId, audience, windowDays);
        FeedbackAggregate agg = aggRepo.findByTargetIdAndWindowDays(targetId, windowDays).orElse(null);
        FeedbackSummaryDTO dto = new FeedbackSummaryDTO();
        dto.targetId = targetId;
        dto.avg = agg != null ? agg.getAvgRating() : null;
        dto.count = agg != null ? agg.getRatingCount() : 0;
        dto.decayedAvg = agg != null ? agg.getDecayedAvgRating() : null;

        // Compute distribution over window
        Instant now = Instant.now();
        Instant start = now.minus(Duration.ofDays(windowDays));
        Page<Feedback> page = feedbackRepo.findPublishedByTarget(targetId, start, now, PageRequest.of(0, 500));
        int c1 = 0, c2 = 0, c3 = 0, c4 = 0, c5 = 0;
        for (Feedback f : page) {
            if (f.getOverallRating() == null) continue;
            int r = f.getOverallRating().setScale(0, RoundingMode.HALF_UP).intValue();
            switch (r) {
                case 1 -> c1++;
                case 2 -> c2++;
                case 3 -> c3++;
                case 4 -> c4++;
                case 5 -> c5++;
                default -> {}
            }
        }
        java.util.Map<Integer, Integer> dist = new java.util.HashMap<>();
        dist.put(1, c1); dist.put(2, c2); dist.put(3, c3); dist.put(4, c4); dist.put(5, c5);
        dto.distribution = dist;

        // Lightweight keywords placeholder (can be improved later)
        dto.topKeywords = java.util.List.of();
        return dto;
    }

    @Transactional
    public void recomputeAggregates(Long targetId, Audience audience) {
        computeForWindow(targetId, audience, rules.windowDaysDefault());
        computeForWindow(targetId, audience, rules.windowDaysLong());
        publisher.publishEvent(new FeedbackAggregatesUpdatedEvent(targetId, audience));
        publisher.publishEvent(new BadgeReevaluationRequestedEvent(targetId, audience));
    }

    private void computeForWindow(Long targetId, Audience audience, int windowDays) {
        Instant now = Instant.now();
        Instant start = now.minus(Duration.ofDays(windowDays));
        Page<Feedback> page = feedbackRepo.findPublishedByTarget(targetId, start, now, PageRequest.of(0, 100));
        int count = (int) page.getTotalElements();
        BigDecimal sum = BigDecimal.ZERO;
        for (Feedback f : page) {
            if (f.getOverallRating() != null) sum = sum.add(f.getOverallRating());
        }
        BigDecimal avg = count > 0 ? sum.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP) : null;
        BigDecimal decayed;
        if (windowDays <= 90) decayed = avg;
        else if (windowDays <= 180 && avg != null) decayed = avg.multiply(new BigDecimal("0.6")).setScale(1, RoundingMode.HALF_UP);
        else decayed = avg != null ? avg.multiply(new BigDecimal("0.3")).setScale(1, RoundingMode.HALF_UP) : null;

        FeedbackAggregate agg = aggRepo.findByTargetIdAndWindowDays(targetId, windowDays).orElseGet(FeedbackAggregate::new);
        agg.setTargetId(targetId);
        agg.setAudience(audience);
        agg.setWindowDays(windowDays);
        agg.setAvgRating(avg);
        agg.setRatingCount(count);
        agg.setDecayedAvgRating(decayed);
        agg.setLastComputedAt(now);
        aggRepo.save(agg);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMissionClosed(MissionClosedEvent evt) {
        var mission = missionRepo.findById(evt.missionId()).orElse(null);
        if (mission == null) return;
        Long clientId = mission.getClient() != null ? mission.getClient().getId() : null;
        Long freelancerId = mission.getFreelanceSelectionne() != null ? mission.getFreelanceSelectionne().getId() : null;
        if (clientId != null && freelancerId != null) {
            openWindowOnMissionClosed(evt.missionId(), clientId, freelancerId);
        }
    }
}



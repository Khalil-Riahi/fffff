package com.projet.freelencetinder.servcie;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.projet.freelencetinder.client.flouci.FlouciClient;
import com.projet.freelencetinder.client.flouci.FlouciClient.PaymentLink;
import com.projet.freelencetinder.client.paymee.PaymeeClient;
import com.projet.freelencetinder.client.paymee.PaymeeClient.PaymeeCheckout;
import com.projet.freelencetinder.dto.paiement.MissionPaiementSummaryDTO;
import com.projet.freelencetinder.dto.paiement.TranchePaiementCreateDTO;
import com.projet.freelencetinder.dto.paiement.TranchePaiementResponseDTO;
import com.projet.freelencetinder.exception.BusinessException;
import com.projet.freelencetinder.exception.ResourceNotFoundException;
import com.projet.freelencetinder.mapper.TranchePaiementMapper;
import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.models.PaymentAudit;
import com.projet.freelencetinder.models.TranchePaiement;
import com.projet.freelencetinder.models.TranchePaiement.StatutTranche;
import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.models.WithdrawalMethod;
import com.projet.freelencetinder.models.WithdrawalMethod.Type;
import com.projet.freelencetinder.repository.MissionRepository;
import com.projet.freelencetinder.repository.PaymentAuditRepository;
import com.projet.freelencetinder.repository.TranchePaiementRepository;
import com.projet.freelencetinder.repository.UtilisateurRepository;
import com.projet.freelencetinder.repository.WithdrawalMethodRepository;

@Service
public class EscrowService {

    private static final Logger log = LoggerFactory.getLogger(EscrowService.class);

    private final MissionRepository            missionRepo;
    private final UtilisateurRepository        userRepo;
    private final TranchePaiementRepository    trancheRepo;
    private final TranchePaiementMapper        mapper;
    private final PaymeeClient                 paymee;     // utilisé en mode ESCROW_PAYMEE (phase 2)
    private final PaymentAuditRepository       auditRepo;
    private final ApplicationEventPublisher    publisher;

    // Nouveaux (MVP direct)
    private final FlouciClient                 flouci;
    private final WithdrawalMethodRepository   methodRepo;

    @Value("${payment.mode:DIRECT_FLOUCI}")
    private String paymentMode; // "DIRECT_FLOUCI" | "ESCROW_PAYMEE"

    @Value("${flouci.mock:false}")
    private boolean flouciMock;

    public EscrowService(MissionRepository missionRepo,
                         UtilisateurRepository userRepo,
                         TranchePaiementRepository trancheRepo,
                         TranchePaiementMapper mapper,
                         PaymeeClient paymee,
                         PaymentAuditRepository auditRepo,
                         ApplicationEventPublisher publisher,
                         FlouciClient flouci,
                         WithdrawalMethodRepository methodRepo) {
        this.missionRepo  = missionRepo;
        this.userRepo     = userRepo;
        this.trancheRepo  = trancheRepo;
        this.mapper       = mapper;
        this.paymee       = paymee;
        this.auditRepo    = auditRepo;
        this.publisher    = publisher;
        this.flouci       = flouci;
        this.methodRepo   = methodRepo;
    }

    /* ---------- Création des tranches ---------- */
    @Transactional
    public TranchePaiementResponseDTO createTranche(TranchePaiementCreateDTO dto, Long clientId) {
        Mission mission = missionRepo.findById(dto.getMissionId())
            .orElseThrow(() -> new ResourceNotFoundException("Mission introuvable"));

        if (!mission.getClient().getId().equals(clientId))
            throw new BusinessException("Seul le client propriétaire peut ajouter des tranches");

        if (mission.getFreelanceSelectionne() == null)
            throw new BusinessException("Aucun freelance sélectionné pour cette mission.");

        TranchePaiement tranche = mapper.toEntity(dto);
        tranche.setClient(mission.getClient());
        tranche.setFreelance(mission.getFreelanceSelectionne());
        tranche.setMission(mission);
        tranche.setMontantBrut(dto.getMontantBrut());
        tranche.setDevise(dto.getDevise());

        trancheRepo.save(tranche);
        logEventForTranche(tranche.getId(), "TRANCHE_CREE", "Ordre=" + tranche.getOrdre());
        // Si la mission était prête à clore, une nouvelle tranche la remet en cours
        if (mission.getStatut() == Mission.Statut.PRET_A_CLOTURER) {
            mission.setStatut(Mission.Statut.EN_COURS);
            missionRepo.save(mission);
            logEventForMission(mission.getId(), "MISSION_REOUVERTE", "Ajout nouvelle tranche");
        }
        return mapper.toDto(tranche);
    }

    /* ============================================================
       ==============  MODE DIRECT_FLOUCI (MVP)  ==================
       ============================================================ */

    /** Génère le lien de paiement Flouci (direct vers le freelance). */
    @Transactional
    public TranchePaiementResponseDTO initPaiementDirect(Long trancheId, Long clientId) {
        ensureMode("DIRECT_FLOUCI");

        TranchePaiement t = getAndCheck(trancheId, clientId,
                StatutTranche.EN_ATTENTE_DEPOT, StatutTranche.EN_ATTENTE_PAIEMENT);

        // Détermination du bénéficiaire selon mode (mock vs réel)
        String beneficiary;
        if (flouciMock) {
            // En simulation, on n'exige pas de méthode de retrait
            beneficiary = "MOCK-BENEFICIARY-" + t.getFreelance().getId();
        } else {
            // Méthode principale du freelance (D17 ou RIB)
            WithdrawalMethod method = methodRepo
                    .findByFreelanceIdAndPrincipalTrue(t.getFreelance().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Freelance sans méthode de paiement principale"));

            beneficiary = method.getType() == Type.D17
                    ? method.getWalletNumber()
                    : method.getRib();
        }

        // Montant direct : net = brut (commission=0)
        BigDecimal amount = t.getMontantNetFreelance() != null ? t.getMontantNetFreelance() : t.getMontantBrut();

        PaymentLink link = flouci.createPayment(
            amount, t.getDevise(),
            "Tranche#" + t.getId(),
            beneficiary
        );

        // Marquer dépôt (génération lien) et stocker URL
        t.marquerDepotEffectue(link.token());
        t.setPaymeePaymentUrl(link.url()); // réutilisation champ URL
        trancheRepo.save(t);

        logEventForTranche(t.getId(), "FLOUCI_LINK_GEN", link.url());
        return mapper.toDto(t);
    }

    /** Webhook Flouci (PAID/FAILED/CANCELLED) */
    @Transactional
    public void handleWebhookFlouci(String token, String status) {
        ensureMode("DIRECT_FLOUCI");

        TranchePaiement t = trancheRepo.findByPaymeeCheckoutId(token)
            .orElseThrow(() -> new BusinessException("Paiement inconnu (token)"));

        String st = status == null ? "" : status.trim().toUpperCase();

        if ("PAID".equals(st) || "COMPLETED".equals(st)) {
            // idempotence
            if (t.getStatut() != StatutTranche.VERSEE_FREELANCE) {
                t.marquerVersementEffectue(); // => VERSEE_FREELANCE + dateVersement
                trancheRepo.save(t);
                logEventForTranche(t.getId(), "FLOUCI_PAID", token);

                recomputeMissionStatus(t.getMission().getId());
            }
        } else if ("FAILED".equals(st) || "CANCELLED".equals(st)) {
            // On revient à l'état initial pour permettre de regénérer un lien
            if (t.getStatut() == StatutTranche.EN_ATTENTE_PAIEMENT) {
                t.setStatut(StatutTranche.EN_ATTENTE_DEPOT);
                trancheRepo.save(t);
                logEventForTranche(t.getId(), "FLOUCI_FAILED", st);
            }
        } else {
            log.warn("Webhook Flouci status inconnu: {}", st);
        }
    }

    /* ============================================================
       ==============  MODE ESCROW_PAYMEE (Phase 2)  ===============
       ============================================================ */

    /** Démarre le checkout Paymee (escrow). */
    @Transactional
    public TranchePaiementResponseDTO initPaiement(Long trancheId, Long clientId) {
        ensureMode("ESCROW_PAYMEE");

        TranchePaiement t = getAndCheck(trancheId, clientId, StatutTranche.EN_ATTENTE_DEPOT);
        PaymeeCheckout checkout = paymee.createCheckout(
            t.getMontantBrut(), t.getDevise(), "Tranche#" + t.getId());

        t.marquerDepotEffectue(checkout.id());  // => EN_ATTENTE_PAIEMENT
        t.setPaymeePaymentUrl(checkout.paymentUrl());
        logEventForTranche(t.getId(), "CHECKOUT_GENERE", checkout.paymentUrl());

        return mapper.toDto(t);
    }

    /** Webhook Paymee (escrow). */
    @Transactional
    public void handleWebhookPaiement(String checkoutId, String status) {
        ensureMode("ESCROW_PAYMEE");

        TranchePaiement t = trancheRepo.findByPaymeeCheckoutId(checkoutId)
            .orElseThrow(() -> new BusinessException("Checkout inconnu"));

        if ("PAID".equalsIgnoreCase(status)) {
            if (t.getStatut() == StatutTranche.EN_ATTENTE_PAIEMENT) {
                t.setStatut(StatutTranche.FONDS_BLOQUES);
                logEventForTranche(t.getId(), "PAIEMENT_PAYEE", "CheckoutId=" + checkoutId);
            }
        } else if ("CANCELLED".equalsIgnoreCase(status)) {
            if (t.getStatut() == StatutTranche.EN_ATTENTE_PAIEMENT) {
                t.setStatut(StatutTranche.EN_ATTENTE_DEPOT);
            }
        }
    }

    /** Validation livrable client (escrow) -> capture + versement via event */
    @Transactional
    public TranchePaiementResponseDTO validerLivrable(Long trancheId, Long clientId) {
        ensureMode("ESCROW_PAYMEE");

        TranchePaiement t = getAndCheck(trancheId, clientId, StatutTranche.FONDS_BLOQUES);
        t.marquerLivrableValide();
        logEventForTranche(t.getId(), "LIVRABLE_VALIDE", "");
        trancheRepo.save(t);

        publisher.publishEvent(new CapturePaiementEvent(t.getId()));
        return mapper.toDto(t);
    }

    /** Événement interne (escrow) */
    public record CapturePaiementEvent(Long trancheId) {}

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCapturePaiement(CapturePaiementEvent evt) {
        if (!"ESCROW_PAYMEE".equalsIgnoreCase(paymentMode)) return;
        processCapture(evt.trancheId());
    }

    /** Traitement capture (escrow) dans une nouvelle transaction. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void processCapture(Long trancheId) {
        TranchePaiement t = trancheRepo.findByIdForUpdate(trancheId)
            .orElseThrow(() -> new ResourceNotFoundException("Tranche introuvable (event)"));

        if (t.getStatut() == StatutTranche.VERSEE_FREELANCE) return;
        if (t.getStatut() != StatutTranche.FONDS_BLOQUES &&
            t.getStatut() != StatutTranche.ERREUR_CAPTURE) return;

        try {
            paymee.transferToFreelance(t.getPaymeeCheckoutId());
        } catch (Exception ex) {
            log.error("Erreur lors de la capture Paymee pour la tranche {}", t.getId(), ex);
            t.setStatut(StatutTranche.ERREUR_CAPTURE);
            trancheRepo.save(t);
            logEventForTranche(t.getId(), "CAPTURE_ERREUR", ex.getMessage());
            return;
        }

        t.marquerVersementEffectue();
        logEventForTranche(t.getId(), "CAPTURE_OK", "");

        Utilisateur f = t.getFreelance();
        f.setSoldeEscrow((f.getSoldeEscrow() == null ? BigDecimal.ZERO : f.getSoldeEscrow())
                .add(t.getMontantNetFreelance()));
        userRepo.save(f);

        recomputeMissionStatus(t.getMission().getId());
    }

    /* ---------- Récapitulatif mission (commun) ---------- */
    @Transactional(readOnly = true)
    public MissionPaiementSummaryDTO summary(Long missionId, Long userId) {
        Mission m = missionRepo.findById(missionId)
            .orElseThrow(() -> new ResourceNotFoundException("Mission introuvable"));

        boolean allowed = m.getClient().getId().equals(userId)
                || (m.getFreelanceSelectionne() != null && m.getFreelanceSelectionne().getId().equals(userId));
        if (!allowed) throw new BusinessException("Accès refusé");

        List<TranchePaiement> list = trancheRepo.findByMissionIdOrderByOrdreAsc(missionId);

        BigDecimal totalBrut = list.stream().map(TranchePaiement::getMontantBrut)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalComm = list.stream().map(TranchePaiement::getCommissionPlateforme)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNet = list.stream().map(TranchePaiement::getMontantNetFreelance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        MissionPaiementSummaryDTO dto = new MissionPaiementSummaryDTO();
        dto.setMissionId(missionId);
        dto.setTitreMission(m.getTitre());
        dto.setTotalBrut(totalBrut);
        dto.setTotalCommission(totalComm);
        dto.setTotalNetFreelance(totalNet);
        dto.setTranches(list.stream().map(mapper::toDto).toList());

        // Enrichissements pour UI de clôture
        java.math.BigDecimal paidTotal = list.stream()
            .filter(TranchePaiement::isPaid)
            .map(TranchePaiement::getMontantBrut)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        boolean allReqOk = list.stream()
            .filter(TranchePaiement::isRequired)
            .allMatch(tp -> tp.isPaid() && tp.isDeliveryAccepted());

        Long finalId = list.stream()
            .filter(TranchePaiement::isFinale)
            .map(TranchePaiement::getId)
            .findFirst().orElse(null);

        boolean finalOk = list.stream()
            .anyMatch(tp -> tp.isFinale() && tp.isPaid() && tp.isDeliveryAccepted());

        dto.setClosurePolicy(m.getClosurePolicy());
        dto.setContractTotalAmount(m.getContractTotalAmount());
        dto.setPaidTotal(paidTotal);
        dto.setAllRequiredPaidAndAccepted(allReqOk);
        dto.setFinalTrancheId(finalId);
        dto.setFinalTranchePaidAndAccepted(finalOk);
        dto.setClosedByClient(m.isClosedByClient());
        dto.setClosedByFreelancer(m.isClosedByFreelancer());
        return dto;
    }

    /* ---------- Recompute statut mission (politique de clôture) ---------- */
    @Transactional
    public void recomputeMissionStatus(Long missionId) {
        Mission mission = missionRepo.findById(missionId)
            .orElseThrow(() -> new ResourceNotFoundException("Mission introuvable"));

        List<TranchePaiement> list = trancheRepo.findByMissionIdOrderByOrdreAsc(missionId);

        boolean allRequiredOk = list.stream()
            .filter(TranchePaiement::isRequired)
            .allMatch(tp -> tp.isPaid() && tp.isDeliveryAccepted());

        if (!allRequiredOk) {
            if (mission.getStatut() == Mission.Statut.PRET_A_CLOTURER) {
                mission.setStatut(Mission.Statut.EN_COURS);
                missionRepo.save(mission);
            }
            return;
        }

        mission.setStatut(Mission.Statut.PRET_A_CLOTURER);

        switch (mission.getClosurePolicy()) {
            case FINAL_MILESTONE_REQUIRED -> {
                boolean finalOk = list.stream()
                    .anyMatch(tp -> tp.isFinale() && tp.isPaid() && tp.isDeliveryAccepted());
                if (finalOk) {
                    mission.setStatut(Mission.Statut.TERMINEE);
                    logEventForMission(mission.getId(), "MISSION_TERMINEE", "Policy=FINAL_MILESTONE_REQUIRED");
                }
            }
            case MANUAL_DUAL_CONFIRM -> {
                if (mission.isClosedByClient() && mission.isClosedByFreelancer()) {
                    mission.setStatut(Mission.Statut.TERMINEE);
                    logEventForMission(mission.getId(), "MISSION_TERMINEE", "Policy=MANUAL_DUAL_CONFIRM");
                }
            }
            case CONTRACT_TOTAL_AMOUNT -> {
                java.math.BigDecimal paidTotal = list.stream()
                    .filter(TranchePaiement::isPaid)
                    .map(TranchePaiement::getMontantBrut)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

                if (mission.getContractTotalAmount() != null &&
                    paidTotal != null &&
                    paidTotal.compareTo(mission.getContractTotalAmount()) >= 0) {
                    boolean hasFinalAccepted = list.stream()
                        .anyMatch(tp -> tp.isFinale() && tp.isDeliveryAccepted());
                    if (hasFinalAccepted) {
                        mission.setStatut(Mission.Statut.TERMINEE);
                        logEventForMission(mission.getId(), "MISSION_TERMINEE", "Policy=CONTRACT_TOTAL_AMOUNT");
                    }
                }
            }
        }

        missionRepo.save(mission);
        if (mission.getStatut() == Mission.Statut.TERMINEE) {
            publisher.publishEvent(new com.projet.freelencetinder.servcie.FeedbackEvents.MissionClosedEvent(missionId));
        }
    }

    /* ---------- Mutations de configuration / confirmations ---------- */
    @Transactional
    public Mission updateMissionClosurePolicy(Long missionId, Long clientId, Mission.ClosurePolicy policy) {
        Mission m = missionRepo.findById(missionId)
            .orElseThrow(() -> new ResourceNotFoundException("Mission introuvable"));
        if (!m.getClient().getId().equals(clientId)) throw new BusinessException("Non autorisé");

        m.setClosurePolicy(policy != null ? policy : Mission.ClosurePolicy.FINAL_MILESTONE_REQUIRED);
        missionRepo.save(m);
        recomputeMissionStatus(missionId);
        return m;
    }

    @Transactional
    public Mission updateContractTotalAmount(Long missionId, Long clientId, java.math.BigDecimal amount) {
        Mission m = missionRepo.findById(missionId)
            .orElseThrow(() -> new ResourceNotFoundException("Mission introuvable"));
        if (!m.getClient().getId().equals(clientId)) throw new BusinessException("Non autorisé");

        if (amount == null || amount.signum() < 0) {
            throw new BusinessException("Montant contrat invalide");
        }
        m.setContractTotalAmount(amount);
        missionRepo.save(m);
        recomputeMissionStatus(missionId);
        return m;
    }

    @Transactional
    public Mission confirmCloseByClient(Long missionId, Long clientId) {
        Mission m = missionRepo.findById(missionId)
            .orElseThrow(() -> new ResourceNotFoundException("Mission introuvable"));
        if (!m.getClient().getId().equals(clientId)) throw new BusinessException("Non autorisé");
        m.setClosedByClient(true);
        missionRepo.save(m);
        recomputeMissionStatus(missionId);
        return m;
    }

    @Transactional
    public Mission confirmCloseByFreelancer(Long missionId, Long freelancerId) {
        Mission m = missionRepo.findById(missionId)
            .orElseThrow(() -> new ResourceNotFoundException("Mission introuvable"));
        if (m.getFreelanceSelectionne() == null ||
            !m.getFreelanceSelectionne().getId().equals(freelancerId)) throw new BusinessException("Non autorisé");
        m.setClosedByFreelancer(true);
        missionRepo.save(m);
        recomputeMissionStatus(missionId);
        return m;
    }

    @Transactional
    public TranchePaiementResponseDTO markTrancheFinale(Long trancheId, Long clientId, boolean value) {
        TranchePaiement t = trancheRepo.findByIdForUpdate(trancheId)
            .orElseThrow(() -> new ResourceNotFoundException("Tranche introuvable"));
        if (!t.getClient().getId().equals(clientId)) throw new BusinessException("Non autorisé");

        if (value) {
            List<TranchePaiement> all = trancheRepo.findByMissionIdOrderByOrdreAsc(t.getMission().getId());
            for (TranchePaiement other : all) {
                if (!other.getId().equals(t.getId()) && other.isFinale()) {
                    other.setFinale(false);
                    trancheRepo.save(other);
                }
            }
        }
        t.setFinale(value);
        trancheRepo.save(t);

        recomputeMissionStatus(t.getMission().getId());
        return mapper.toDto(t);
    }

    @Transactional
    public TranchePaiementResponseDTO markTrancheRequired(Long trancheId, Long clientId, boolean value) {
        TranchePaiement t = trancheRepo.findByIdForUpdate(trancheId)
            .orElseThrow(() -> new ResourceNotFoundException("Tranche introuvable"));
        if (!t.getClient().getId().equals(clientId)) throw new BusinessException("Non autorisé");

        t.setRequired(value);
        trancheRepo.save(t);

        recomputeMissionStatus(t.getMission().getId());
        return mapper.toDto(t);
    }


    /* ---------- Helpers ---------- */
    private TranchePaiement getAndCheck(Long id, Long clientId, StatutTranche... etatsAttendus) {
        TranchePaiement t = trancheRepo.findByIdForUpdate(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tranche introuvable"));

        if (!t.getClient().getId().equals(clientId))
            throw new BusinessException("Non autorisé");

        if (etatsAttendus != null && etatsAttendus.length > 0) {
            Set<StatutTranche> set = java.util.EnumSet.noneOf(StatutTranche.class);
            java.util.Collections.addAll(set, etatsAttendus);
            if (!set.contains(t.getStatut()))
                throw new BusinessException("Statut invalide pour l’opération");
        }
        return t;
    }

    private void ensureMode(String expected) {
        if (!expected.equalsIgnoreCase(paymentMode)) {
            throw new BusinessException("Mode paiement incompatible: attendu=" + expected + ", actuel=" + paymentMode);
        }
    }

    private void logEvent(Long trancheId, Long missionId, String event, String details) {
        PaymentAudit a = new PaymentAudit();
        a.setTrancheId(trancheId);
        a.setMissionId(missionId);
        a.setEvent(event);
        a.setDetails(details);
        auditRepo.save(a);
    }

    private void logEventForTranche(Long trancheId, String event, String details) {
        logEvent(trancheId, null, event, details);
    }

    private void logEventForMission(Long missionId, String event, String details) {
        logEvent(null, missionId, event, details);
    }

    /** Retry capture déclenché par le scheduler (n’a d’effet qu’en escrow). */
    @Transactional
    public void retryCapture(Long trancheId) {
        if (!"ESCROW_PAYMEE".equalsIgnoreCase(paymentMode)) {
            log.debug("[DIRECT] retryCapture ignoré (mode={})", paymentMode);
            return;
        }
        publisher.publishEvent(new CapturePaiementEvent(trancheId));
    }
}

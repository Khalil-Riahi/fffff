package com.projet.freelencetinder.servcie; // ou com.projet.freelencetinder.service

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projet.freelencetinder.dto.ClientInfoDTO;
import com.projet.freelencetinder.dto.FreelanceSummaryDTO;
import com.projet.freelencetinder.dto.MissionRecommendationDTO;
import com.projet.freelencetinder.dto.MissionSummaryDTO;
import com.projet.freelencetinder.dto.MatchNotification;
import com.projet.freelencetinder.models.ClientSwipe;
import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.models.Mission.Categorie;
import com.projet.freelencetinder.models.Mission.Statut;
import com.projet.freelencetinder.models.Swipe;
import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.models.Utilisateur.TypeUtilisateur;
import com.projet.freelencetinder.repository.ClientSwipeRepository;
import com.projet.freelencetinder.repository.MissionRepository;
import com.projet.freelencetinder.repository.SwipeRepository;
import com.projet.freelencetinder.repository.UtilisateurRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SwipeService {

    private final MissionRepository missionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SwipeRepository swipeRepository;
    private final ClientSwipeRepository clientSwipeRepository;
    private final ConversationService   conversationService;
    private final SimpMessagingTemplate broker;
    private final com.projet.freelencetinder.servcie.CompetenceService competenceService;

    @Autowired
    public SwipeService(MissionRepository missionRepository,
                        UtilisateurRepository utilisateurRepository,
                        SwipeRepository swipeRepository,
                        ClientSwipeRepository clientSwipeRepository,
                        ConversationService conversationService,
                        SimpMessagingTemplate broker,
                        com.projet.freelencetinder.servcie.CompetenceService competenceService) {
        this.missionRepository      = missionRepository;
        this.utilisateurRepository  = utilisateurRepository;
        this.swipeRepository        = swipeRepository;
        this.clientSwipeRepository  = clientSwipeRepository;
        this.conversationService    = conversationService;
        this.broker                 = broker;
        this.competenceService      = competenceService;
    }

    /* =============================================================
       LISTE DES MISSIONS POUR SWIPE FREELANCE
       ============================================================= */
    public List<MissionSummaryDTO> getMissionsForSwipe(Long freelanceId, Categorie categorie) {
        Utilisateur freelance = getFreelanceOrThrow(freelanceId);

        Set<Long> dejaSwipes = swipeRepository.findByFreelanceId(freelanceId).stream()
                .map(s -> s.getMission().getId())
                .collect(Collectors.toSet());

        LocalDate today = LocalDate.now();

        return missionRepository.findAll().stream()
                .filter(Mission::estDisponiblePourSwipe)
                .filter(m -> !dejaSwipes.contains(m.getId()))
                .filter(m -> categorie == null || m.getCategorie() == categorie)
                .map(m -> toSummaryDTO(m, today))
                .collect(Collectors.toList());
    }

    /* =============================================================
       SWIPE FREELANCE -> MISSION
       ============================================================= */
    @Transactional
    public Swipe swipeMission(Long freelanceId, Long missionId, Swipe.Decision decision) {
        return swipeMission(freelanceId, missionId, decision, null);
    }

    // surcharge avec dwellTimeMs (analytics front)
    @Transactional
    public Swipe swipeMission(Long freelanceId, Long missionId, Swipe.Decision decision, Long dwellTimeMs) {

        Utilisateur freelance = getFreelanceOrThrow(freelanceId);
        Mission     mission   = getMissionOrThrow(missionId);

        if (mission.getClient() != null && mission.getClient().getId().equals(freelanceId)) {
            throw new IllegalStateException("Un freelance ne peut pas swiper sa propre mission");
        }
        if (!mission.estDisponiblePourSwipe()) {
            throw new IllegalStateException("Mission non disponible pour swipe (statut=" + mission.getStatut() + ")");
        }
        if (swipeRepository.findByFreelanceIdAndMissionId(freelanceId, missionId).isPresent()) {
            throw new IllegalStateException("Mission déjà swipée par ce freelance");
        }

        Swipe swipe = new Swipe();
        swipe.setFreelance(freelance);
        swipe.setMission(mission);
        swipe.setDecision(decision);
        swipe.setDateSwipe(LocalDateTime.now());
        if (dwellTimeMs != null && dwellTimeMs >= 0) {
            swipe.setDwellTimeMs(dwellTimeMs);
        }

        swipeRepository.save(swipe);

        mission.incrementSwipe();
        freelance.incrementNombreSwipes();
        if (decision == Swipe.Decision.LIKE) mission.incrementLike();

        if (decision == Swipe.Decision.LIKE) {
            Utilisateur client = mission.getClient();
            boolean clientLike = clientSwipeRepository
                    .findByClientIdAndMissionIdAndFreelanceId(client.getId(), missionId, freelanceId)
                    .map(c -> c.getDecision() == Swipe.Decision.LIKE)
                    .orElse(false);

            if (clientLike && mission.getStatut() == Statut.EN_ATTENTE) {
                handleMatch(mission, client, freelance, swipe, null);
            }
        }
        return swipe;
    }

    /* =============================================================
       SWIPE CLIENT -> FREELANCE
       ============================================================= */
    @Transactional
    public ClientSwipe clientSwipeFreelance(Long clientId,
                                            Long missionId,
                                            Long freelanceId,
                                            Swipe.Decision decision) {
        return clientSwipeFreelance(clientId, missionId, freelanceId, decision, null);
    }

    // surcharge avec dwellTimeMs (analytics front)
    @Transactional
    public ClientSwipe clientSwipeFreelance(Long clientId,
                                            Long missionId,
                                            Long freelanceId,
                                            Swipe.Decision decision,
                                            Long dwellTimeMs) {

        Utilisateur client    = getClientOrThrow(clientId);
        Mission     mission   = getMissionOrThrow(missionId);

        if (!mission.getClient().getId().equals(clientId)) {
            throw new IllegalArgumentException("Cette mission n’appartient pas à ce client");
        }
        if (mission.getStatut() != Statut.EN_ATTENTE || mission.isVerrouillee()) {
            throw new IllegalStateException("Mission verrouillée ou engagée");
        }

        Utilisateur freelance = getFreelanceOrThrow(freelanceId);

        if (clientSwipeRepository
                .findByClientIdAndMissionIdAndFreelanceId(clientId, missionId, freelanceId)
                .isPresent()) {
            throw new IllegalStateException("Ce freelance a déjà été swipé par le client");
        }

        ClientSwipe cs = new ClientSwipe();
        cs.setClient(client);
        cs.setMission(mission);
        cs.setFreelance(freelance);
        cs.setDecision(decision);
        cs.setDateSwipe(LocalDateTime.now());
        if (dwellTimeMs != null && dwellTimeMs >= 0) {
            cs.setDwellTimeMs(dwellTimeMs);
        }

        clientSwipeRepository.save(cs);

        if (decision == Swipe.Decision.LIKE) freelance.incrementLikesRecus();

        if (decision == Swipe.Decision.LIKE
                && mission.getStatut() == Statut.EN_ATTENTE
                && !mission.isVerrouillee()) {

            boolean freelanceLike = swipeRepository
                    .findByFreelanceIdAndMissionId(freelanceId, missionId)
                    .map(s -> s.getDecision() == Swipe.Decision.LIKE)
                    .orElse(false);

            if (freelanceLike) {
               handleMatch(mission, client, freelance, null, cs);
            }
        }
        return cs;
    }

    /* ============================================================================
       Gestion centralisée d’un MATCH (freelance + client LIKE)
       ============================================================================ */
    private void handleMatch(Mission mission,
                             Utilisateur client,
                             Utilisateur freelance,
                             Swipe swipe,
                             ClientSwipe clientSwipe)
    {
      try {
          mission.affecterFreelance(freelance);
          freelance.incrementMatchesObtenus();
          if (swipe       != null) swipe.setAGenereMatch(true);
          if (clientSwipe != null) clientSwipe.setAGenereMatch(true);
          missionRepository.save(mission);

          var conv = conversationService
                  .findOrCreate(mission.getId(),
                                 client.getId(),
                                 freelance.getId());

          MatchNotification notif = new MatchNotification(
              conv.getId(), mission.getId(),
              client.getId(), freelance.getId(),
              mission.getTitre(),
              client.getNomComplet(), freelance.getNomComplet(),
              client.getPhotoProfilUrl(),
              freelance.getPhotoProfilUrl()
          );

          broker.convertAndSendToUser(client.getEmail(), "/queue/matches", notif);
          broker.convertAndSendToUser(freelance.getEmail(), "/queue/matches", notif);

      } catch (OptimisticLockingFailureException ex) {
          throw new IllegalStateException("Conflit de mise à jour (match concurrent)", ex);
      }
    }

    /* =============================================================
       ASSIGNATION DIRECTE (ADMIN / CLIENT)
       ============================================================= */
    @Transactional
    public void assignFreelanceToMission(Long missionId, Long freelanceId) {
        Mission mission = getMissionOrThrow(missionId);
        Utilisateur freelance = getFreelanceOrThrow(freelanceId);

        if (mission.getStatut() != Statut.EN_ATTENTE || mission.isVerrouillee()) {
            throw new IllegalStateException("La mission n’est plus disponible");
        }

        mission.affecterFreelance(freelance);
        missionRepository.save(mission);
        freelance.incrementMatchesObtenus();
    }

    /* =============================================================
       SUPPRESSION SWIPE FREELANCE
       ============================================================= */
    @Transactional
    public void removeSwipeFreelance(Long freelanceId, Long missionId) {
        Swipe swipe = swipeRepository
                .findByFreelanceIdAndMissionId(freelanceId, missionId)
                .orElseThrow(() -> new EntityNotFoundException("Aucun swipe trouvé"));

        swipeRepository.delete(swipe);

        Mission mission = swipe.getMission();
        if (swipe.getDecision() == Swipe.Decision.LIKE
                && mission.getFreelanceSelectionne() != null
                && mission.getFreelanceSelectionne().getId().equals(freelanceId)
                && mission.getStatut() == Statut.EN_COURS) {

            mission.setFreelanceSelectionne(null);
            mission.setStatut(Statut.EN_ATTENTE);
            mission.setVerrouillee(false);
            missionRepository.save(mission);
        }
    }

    /* =============================================================
       SUPPRESSION SWIPE CLIENT
       ============================================================= */
    @Transactional
    public void removeSwipeClient(Long clientId, Long missionId, Long freelanceId) {
        ClientSwipe cs = clientSwipeRepository
                .findByClientIdAndMissionIdAndFreelanceId(clientId, missionId, freelanceId)
                .orElseThrow(() -> new EntityNotFoundException("Aucun swipe client trouvé"));

        clientSwipeRepository.delete(cs);

        Mission mission = cs.getMission();
        if (cs.getDecision() == Swipe.Decision.LIKE
                && mission.getFreelanceSelectionne() != null
                && mission.getFreelanceSelectionne().getId().equals(freelanceId)
                && mission.getStatut() == Statut.EN_COURS) {

            mission.setFreelanceSelectionne(null);
            mission.setStatut(Statut.EN_ATTENTE);
            mission.setVerrouillee(false);
            missionRepository.save(mission);
        }
    }

    /* =============================================================
       RECOMMANDATIONS POUR FREELANCE
       ============================================================= */
    public List<MissionRecommendationDTO> getRecommandationsPourFreelance(Long freelanceId) {
        Utilisateur freelance = getFreelanceOrThrow(freelanceId);

        Set<Long> dejaSwipes = swipeRepository.findByFreelanceId(freelanceId).stream()
                .map(s -> s.getMission().getId())
                .collect(Collectors.toSet());

        Set<Mission.Categorie> categoriesPref = swipeRepository
                .findByFreelanceIdAndDecision(freelanceId, Swipe.Decision.LIKE)
                .stream()
                .map(s -> s.getMission().getCategorie())
                .collect(Collectors.toSet());

        LocalDate today = LocalDate.now();

        return missionRepository.findAll().stream()
                .filter(Mission::estDisponiblePourSwipe)
                .filter(m -> !dejaSwipes.contains(m.getId()))
                .filter(m -> hasSkillOverlap(m, freelance))
                .sorted(Comparator.comparingInt(
                        (Mission m) -> scoreMissionPourFreelance(m, freelance, categoriesPref))
                        .reversed())
                .limit(40)
                .map(m -> {
                    int sc = scoreMissionPourFreelance(m, freelance, categoriesPref);
                    return toRecommendationDTO(m, freelance, sc, today);
                })
                .collect(Collectors.toList());
    }

    /* =============================================================
       MAPPINGS DTO
       ============================================================= */
    private MissionSummaryDTO toSummaryDTO(Mission m, LocalDate today) {
        MissionSummaryDTO dto = new MissionSummaryDTO();
        dto.setId(m.getId());
        dto.setTitre(m.getTitre());
        dto.setBudget(m.getBudget());
        dto.setDevise(m.getDevise());
        dto.setCategorie(m.getCategorie());
        dto.setStatut(m.getStatut());
        dto.setDatePublication(m.getDatePublication());
        dto.setDureeEstimeeJours(m.getDureeEstimeeJours());
        dto.setDateLimiteCandidature(m.getDateLimiteCandidature());
        dto.setModaliteTravail(m.getModaliteTravail());

        boolean expired = m.estExpirée();
        dto.setExpired(expired);
        dto.setUrgent(!expired
                && m.getDateLimiteCandidature() != null
                && ChronoUnit.DAYS.between(today, m.getDateLimiteCandidature()) <= 3);

        dto.setClient(toClientInfoDTO(m.getClient()));
        return dto;
    }

    private MissionRecommendationDTO toRecommendationDTO(Mission m,
                                                         Utilisateur freelance,
                                                         int score,
                                                         LocalDate today) {
        MissionRecommendationDTO dto = new MissionRecommendationDTO();
        dto.setId(m.getId());
        dto.setTitre(m.getTitre());
        dto.setBudget(m.getBudget());
        dto.setDevise(m.getDevise());
        dto.setCategorie(m.getCategorie());
        dto.setStatut(m.getStatut());
        dto.setDatePublication(m.getDatePublication());
        dto.setDureeEstimeeJours(m.getDureeEstimeeJours());
        dto.setDateLimiteCandidature(m.getDateLimiteCandidature());
        dto.setModaliteTravail(m.getModaliteTravail());
        dto.setScore(score);

        int totalReq = m.getCompetencesRequises() != null ? m.getCompetencesRequises().size() : 0;
        dto.setTotalRequiredSkills(totalReq);
        int matched = (int) (m.getCompetencesRequises() == null ? 0 :
                m.getCompetencesRequises().stream()
                        .filter(freelance.getCompetences()::contains)
                        .count());
        dto.setMatchedSkills(matched);
        dto.setMatchRatio(totalReq == 0 ? 0.0 : (double) matched / totalReq);

        boolean expired = m.estExpirée();
        dto.setExpired(expired);
        dto.setUrgent(!expired
                && m.getDateLimiteCandidature() != null
                && ChronoUnit.DAYS.between(today, m.getDateLimiteCandidature()) <= 3);

        dto.setAlreadySwiped(false);
        dto.setLikedByCurrentUser(false);
        dto.setMutualMatch(false);

        dto.setClient(toClientInfoDTO(m.getClient()));
        return dto;
    }

    private ClientInfoDTO toClientInfoDTO(Utilisateur client) {
        if (client == null) return null;
        ClientInfoDTO dto = new ClientInfoDTO();
        dto.setId(client.getId());
        dto.setNom(client.getNom());
        dto.setPrenom(client.getPrenom());
        dto.setPhotoUrl(client.getPhotoProfilUrl());
        dto.setVille(client.getLocalisation());
        dto.setTypeClient(client.getTypeClient());

        // ===== AJOUTS : champs publics côté client =====
        dto.setGouvernorat(client.getGouvernorat());
        dto.setTimezone(client.getTimezone());

        // Réputation / stats client
        dto.setMissionsPubliees(client.getMissionsPubliees());
        dto.setNoteDonneeMoy(client.getNoteDonneeMoy());
        dto.setFiabilitePaiement(client.getFiabilitePaiement());
        dto.setDelaiPaiementMoyenJours(client.getDelaiPaiementMoyenJours());

        // KYC / vérifications
        dto.setEmailVerifie(client.isEmailVerifie());
        dto.setTelephoneVerifie(client.isTelephoneVerifie());
        dto.setIdentiteVerifiee(client.isIdentiteVerifiee());
        dto.setRibVerifie(client.isRibVerifie());
        dto.setKycStatut(client.getKycStatut());

        // Entreprise
        dto.setNomEntreprise(client.getNomEntreprise());
        dto.setSiteEntreprise(client.getSiteEntreprise());
        dto.setDescriptionEntreprise(client.getDescriptionEntreprise());

        // Badges
        if (client.getListeBadges() != null && !client.getListeBadges().isEmpty()) {
            dto.setBadges(client.getListeBadges());
        }
        return dto;
    }

    /* =============================================================
       LOGIQUE SCORING
       ============================================================= */
    private boolean hasSkillOverlap(Mission m, Utilisateur freelance) {
        if (m.getCompetencesRequises() == null || m.getCompetencesRequises().isEmpty()) return true;
        if (freelance.getCompetences() == null) return false;

        // Canonicalize both sides for robust matching (case/accents)
        java.util.Set<String> missionCanon = m.getCompetencesRequises().stream()
                .map(competenceService::toCanonical)
                .collect(java.util.stream.Collectors.toSet());
        java.util.Set<String> freelancerCanon = freelance.getCompetences().stream()
                .map(competenceService::toCanonical)
                .collect(java.util.stream.Collectors.toSet());
        return missionCanon.stream().anyMatch(freelancerCanon::contains);
    }

    private int scoreMissionPourFreelance(Mission m, Utilisateur freelance, Set<Mission.Categorie> categoriesPref) {
        int score = 0;
        long commonSkills = m.getCompetencesRequises() != null
                ? m.getCompetencesRequises().stream()
                .filter(freelance.getCompetences()::contains).count()
                : 0;
        score += commonSkills * 4;
        if (categoriesPref.contains(m.getCategorie())) score += 5;
        if (freelance.getTarifHoraire() == null
                || (m.getBudget() != null && freelance.getTarifHoraire() != null
                && m.getBudget().doubleValue() >= freelance.getTarifHoraire())) {
            score += 2;
        }
        long ageDays = ChronoUnit.DAYS.between(m.getDatePublication(), LocalDateTime.now());
        score += Math.max(0, 21 - ageDays);
        if (m.getDateLimiteCandidature() != null) {
            long toDeadline = ChronoUnit.DAYS.between(LocalDate.now(), m.getDateLimiteCandidature());
            if (toDeadline <= 7 && toDeadline >= 0) score += 2;
        }
        return score;
    }

    /* =============================================================
       HELPERS ENTITÉS
       ============================================================= */
    private Mission getMissionOrThrow(Long missionId) {
        return missionRepository.findById(missionId)
                .orElseThrow(() -> new EntityNotFoundException("Mission introuvable (id=" + missionId + ")"));
    }

    private Utilisateur getFreelanceOrThrow(Long userId) {
        Utilisateur u = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Freelance introuvable (id=" + userId + ")"));
        if (u.getTypeUtilisateur() != TypeUtilisateur.FREELANCE) {
            throw new IllegalArgumentException("L’utilisateur n’est pas un freelance");
        }
        return u;
    }

    private Utilisateur getClientOrThrow(Long userId) {
        Utilisateur u = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable (id=" + userId + ")"));
        if (u.getTypeUtilisateur() != TypeUtilisateur.CLIENT) {
            throw new IllegalArgumentException("L’utilisateur n’est pas un client");
        }
        return u;
    }

    /* =============================================================
       FREELANCES QUI ONT LIKÉ UNE MISSION (vue client)
       ============================================================= */
    public List<FreelanceSummaryDTO> getFreelancersWhoLikedMission(Long clientId, Long missionId) {
        Utilisateur client = getClientOrThrow(clientId);
        Mission mission    = getMissionOrThrow(missionId);

        if (!mission.getClient().getId().equals(clientId)) {
            throw new IllegalArgumentException("Cette mission n’appartient pas à ce client");
        }

        Set<Long> dejaSwipes = clientSwipeRepository
                .findByClientIdAndMissionId(clientId, missionId)
                .stream()
                .map(cs -> cs.getFreelance().getId())
                .collect(Collectors.toSet());

        return swipeRepository
                .findByMissionIdAndDecision(missionId, Swipe.Decision.LIKE)
                .stream()
                .map(Swipe::getFreelance)
                .filter(f -> !dejaSwipes.contains(f.getId()))
                .map(this::toFreelanceSummaryDTO)
                .collect(Collectors.toList());
    }

    /* -------------------------------------------------------------
       Mapping Freelance -> FreelanceSummaryDTO (ENRICHI)
       ------------------------------------------------------------- */
    private FreelanceSummaryDTO toFreelanceSummaryDTO(Utilisateur f) {
        FreelanceSummaryDTO dto = new FreelanceSummaryDTO();
        dto.setId(f.getId());
        dto.setNom(f.getNom());
        dto.setPrenom(f.getPrenom());
        dto.setPhotoUrl(f.getPhotoProfilUrl());
        dto.setLocalisation(f.getLocalisation());
        dto.setGouvernorat(f.getGouvernorat()); // AJOUT
        dto.setNiveauExperience(f.getNiveauExperience() != null ? f.getNiveauExperience().name() : null);
        dto.setDisponibilite(f.getDisponibilite() != null ? f.getDisponibilite().name() : null);
        dto.setTarifHoraire(f.getTarifHoraire());
        dto.setTarifJournalier(f.getTarifJournalier());
        dto.setNoteMoyenne(f.getNoteMoyenne());
        dto.setCompetences(f.getCompetences());
        dto.setPortfolioUrls(f.getPortfolioUrls()); // AJOUT

        dto.setBadgePrincipal(
            f.getListeBadges() != null && !f.getListeBadges().isEmpty()
                ? f.getListeBadges().iterator().next()
                : null
        );

        // Champs “carte” supplémentaires
        dto.setTitreProfil(f.getTitreProfil());
        dto.setAnneesExperience(f.getAnneesExperience());
        dto.setMobilite(f.getMobilite());
        dto.setTimezone(f.getTimezone());
        dto.setModelesEngagementPreferes(
            f.getModelesEngagementPreferes() == null ? null : new java.util.ArrayList<>(f.getModelesEngagementPreferes())
        );
        dto.setDateDisponibilite(f.getDateDisponibilite() != null ? f.getDateDisponibilite().toString() : null);
        dto.setChargeHebdoSouhaiteeJours(f.getChargeHebdoSouhaiteeJours());
        dto.setLangues(f.getLangues());
        dto.setCompetencesNiveaux(f.getCompetencesNiveaux());
        dto.setTauxReussite(f.getTauxReussite());
        dto.setTauxRespectDelais(f.getTauxRespectDelais());
        dto.setTauxReembauche(f.getTauxReembauche());
        dto.setDelaiReponseHeures(f.getDelaiReponseHeures());
        dto.setDelaiReponseMedianMinutes(f.getDelaiReponseMedianMinutes());
        dto.setCertifications(f.getCertifications());

        // KYC / vérifs
        dto.setEmailVerifie(f.isEmailVerifie());
        dto.setTelephoneVerifie(f.isTelephoneVerifie());
        dto.setIdentiteVerifiee(f.isIdentiteVerifiee());
        dto.setRibVerifie(f.isRibVerifie());
        dto.setKycStatut(f.getKycStatut());

        // Nouveaux champs
        dto.setPreferenceDuree(f.getPreferenceDuree());
        dto.setNombreAvis(f.getNombreAvis());
        dto.setLinkedinUrl(f.getLinkedinUrl());
        dto.setGithubUrl(f.getGithubUrl());

        return dto;
    }

    /* =============================================================
       EXPLORE FREELANCES POUR UNE MISSION (match par catégorie)
       ============================================================= */
    @Transactional(readOnly = true)
    public List<FreelanceSummaryDTO> getFreelancersMatchingMission(Long clientId, Long missionId) {

        Utilisateur client  = getClientOrThrow(clientId);
        Mission     mission = getMissionOrThrow(missionId);
        if (!mission.getClient().getId().equals(clientId)) {
            throw new IllegalArgumentException("Cette mission n’appartient pas à ce client");
        }

        Mission.Categorie categorie = mission.getCategorie();

        Set<Long> dejaSwipes = clientSwipeRepository
                .findByClientIdAndMissionId(clientId, missionId)
                .stream()
                .map(cs -> cs.getFreelance().getId())
                .collect(Collectors.toSet());

        Long dejaAffecte = mission.getFreelanceSelectionne() != null
                ? mission.getFreelanceSelectionne().getId() : null;

        return utilisateurRepository.findAll().stream()
            .filter(u -> u.getTypeUtilisateur() == TypeUtilisateur.FREELANCE && u.isEstActif())
            .filter(u -> u.getCategories() != null && u.getCategories().contains(categorie))
            .filter(u -> !dejaSwipes.contains(u.getId()))
            .filter(u -> !Objects.equals(u.getId(), dejaAffecte))
            .map(this::toFreelanceSummaryDTO)
            .collect(Collectors.toList());
    }
}

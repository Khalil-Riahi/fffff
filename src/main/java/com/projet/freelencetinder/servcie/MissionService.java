package com.projet.freelencetinder.servcie;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.projet.freelencetinder.dto.ClientInfoDTO;
import com.projet.freelencetinder.dto.FreelanceSummaryDTO;
import com.projet.freelencetinder.dto.MissionCardDto;
import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.models.Mission.ModaliteTravail;
import com.projet.freelencetinder.models.Mission.Statut;
import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.repository.LivrableRepository;
import com.projet.freelencetinder.repository.MissionRepository;
import com.projet.freelencetinder.repository.TranchePaiementRepository;
import com.projet.freelencetinder.repository.UtilisateurRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Service focalisé sur la gestion "pure" des missions (CRUD & requêtes).
 * Toute la logique de swipe / matching / scoring est déportée dans SwipeService.
 */
@Service
public class MissionService {

    private final MissionRepository missionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final com.projet.freelencetinder.servcie.CompetenceService competenceService;
    private static final Logger log = LoggerFactory.getLogger(MissionService.class);
    private final LivrableRepository livrableRepository;
    private final TranchePaiementRepository trancheRepository;
    private final EscrowService escrowService;

    @Autowired
    public MissionService(MissionRepository missionRepository,
                          UtilisateurRepository utilisateurRepository,
                          com.projet.freelencetinder.servcie.CompetenceService competenceService,
                          LivrableRepository livrableRepository,
                          TranchePaiementRepository trancheRepository,
                          EscrowService escrowService) {
        this.missionRepository     = missionRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.competenceService     = competenceService;
        this.livrableRepository    = livrableRepository;
        this.trancheRepository     = trancheRepository;
        this.escrowService         = escrowService;
    }

    /* ------------------------------------------------------------------
       1. Création d’une mission
       ------------------------------------------------------------------ */
    @Transactional
    public Mission createMission(Mission mission) {
        if (mission.getClient() == null || mission.getClient().getId() == null) {
            throw new IllegalArgumentException("Client requis pour créer une mission");
        }

        // 🔥 Recharger le client à partir de la base pour qu’il soit "attaché"
        Utilisateur clientAttaché = utilisateurRepository.findById(mission.getClient().getId())
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable (id=" + mission.getClient().getId() + ")"));

        mission.setClient(clientAttaché); // ✅ maintenant c'est un managed entity

        // AJOUT : normalisation défauts Tunisie & garde-fous
        if (!StringUtils.hasText(mission.getDevise())) mission.setDevise("TND");
        if (mission.getBudget() == null || mission.getBudget().compareTo(BigDecimal.ZERO) < 0)
            mission.setBudget(BigDecimal.ZERO);
        if (mission.getModaliteTravail() == null) mission.setModaliteTravail(ModaliteTravail.NON_SPECIFIE);
        if (mission.getStatut() == null) mission.setStatut(Statut.EN_ATTENTE);

        // Normalisation douce compétences requises
        if (mission.getCompetencesRequises() != null && !mission.getCompetencesRequises().isEmpty()) {
            java.util.Set<String> normalized = competenceService.normalizeSet(mission.getCompetencesRequises());
            if (!normalized.equals(mission.getCompetencesRequises())) {
                log.info("[Mission#create] Compétences normalisées: {} -> {}", mission.getCompetencesRequises(), normalized);
            }
            mission.setCompetencesRequises(normalized);
        }

        // AJOUT : validation centralisée
        validateMission(mission, true);

        return missionRepository.save(mission);
    }

    /* ------------------------------------------------------------------
       2. Listing global (prototype) – à remplacer par pagination
       ------------------------------------------------------------------ */
    @Transactional(readOnly = true)
    public List<Mission> getAllMissions() {
        return missionRepository.findAll();
    }

    /* ------------------------------------------------------------------
       3. Détails
       ------------------------------------------------------------------ */
    @Transactional(readOnly = true)
    public Mission getMissionById(Long id) {
        return missionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Mission introuvable avec l’id " + id));
    }

    /* ------------------------------------------------------------------
       4. Mise à jour (client propriétaire)
       ------------------------------------------------------------------ */
    @Transactional
    public Mission updateMission(Long id, Mission dto) {
        Mission existing = getMissionById(id);

        if (existing.getStatut() != Statut.EN_ATTENTE) {
            throw new IllegalStateException("La mission ne peut plus être modifiée (statut=" + existing.getStatut() + ")");
        }

        // Mise à jour "safe" : on écrase uniquement par des valeurs non null / non vides
        if (StringUtils.hasText(dto.getTitre())) existing.setTitre(dto.getTitre());
        if (StringUtils.hasText(dto.getDescription())) existing.setDescription(dto.getDescription());
        if (dto.getCompetencesRequises() != null) {
            java.util.Set<String> normalized = competenceService.normalizeSet(dto.getCompetencesRequises());
            log.info("[Mission#update] Normalisation compétences: {} -> {}", dto.getCompetencesRequises(), normalized);
            existing.setCompetencesRequises(normalized);
        }

        if (dto.getBudget() != null && dto.getBudget().compareTo(BigDecimal.ZERO) >= 0)
            existing.setBudget(dto.getBudget());
        if (StringUtils.hasText(dto.getDevise())) existing.setDevise(dto.getDevise());

        if (dto.getDelaiLivraison() != null) existing.setDelaiLivraison(dto.getDelaiLivraison());
        if (dto.getDureeEstimeeJours() != null) existing.setDureeEstimeeJours(dto.getDureeEstimeeJours());
        if (dto.getDateLimiteCandidature() != null) existing.setDateLimiteCandidature(dto.getDateLimiteCandidature());

        if (StringUtils.hasText(dto.getLocalisation())) existing.setLocalisation(dto.getLocalisation());
        if (dto.getModaliteTravail() != null) existing.setModaliteTravail(dto.getModaliteTravail());

        if (dto.getCategorie() != null) existing.setCategorie(dto.getCategorie());

        // NB: statut modifiable uniquement si EN_ATTENTE -> rester prudent
        if (dto.getStatut() != null && dto.getStatut() == Statut.EN_ATTENTE) {
            existing.setStatut(dto.getStatut());
        }

        /* ---------- champs existants (déjà présents dans ton code) ---------- */
        if (dto.getMediaUrls() != null) existing.setMediaUrls(dto.getMediaUrls());
        if (StringUtils.hasText(dto.getVideoBriefUrl())) existing.setVideoBriefUrl(dto.getVideoBriefUrl());

        // AJOUT : validation et garde-fous de cohérence
        validateMission(existing, false);

        return missionRepository.save(existing);
    }

    /* ------------------------------------------------------------------
       5. Suppression
       ------------------------------------------------------------------ */
    @Transactional
    public void deleteMission(Long id) {
        Mission m = getMissionById(id);
        if (m.getStatut() != Statut.EN_ATTENTE && m.getStatut() != Statut.ANNULEE) {
            throw new IllegalStateException("Impossible de supprimer une mission déjà engagée");
        }
        missionRepository.delete(m);
    }

    /* ------------------------------------------------------------------
       9. Missions d’un client
       ------------------------------------------------------------------ */
    @Transactional(readOnly = true)
    public List<Mission> getMissionsByClient(Long clientId) {
        return missionRepository.findAll().stream()
                .filter(m -> m.getClient() != null && m.getClient().getId().equals(clientId))
                .collect(Collectors.toList());
    }

    /* ------------------------------------------------------------------
       9.b Cartes Mission enrichies pour un client
       ------------------------------------------------------------------ */
    @Transactional(readOnly = true)
    public List<MissionCardDto> getMissionCardsByClient(Long clientId) {
        List<Mission> missions = missionRepository.findByClientId(clientId);
        if (missions.isEmpty()) return List.of();

        List<Long> missionIds = missions.stream().map(Mission::getId).toList();

        // Agrégations uniques pour KPI
        var livAggList = livrableRepository.aggregateByMissionIds(missionIds);
        var trancheDueList = trancheRepository.trancheDueByMissionIds(missionIds);

        java.util.Map<Long, LivrableRepository.LivrableAgg> livAggByMission = new java.util.HashMap<>();
        for (var a : livAggList) livAggByMission.put(a.getMissionId(), a);

        java.util.Map<Long, Long> trancheDueByMission = new java.util.HashMap<>();
        for (var td : trancheDueList) trancheDueByMission.put(td.getMissionId(), td.getTrancheIdDue());

        return missions.stream().map(m -> {
            MissionCardDto d = new MissionCardDto();
            // Mapping infos de base
            d.setId(m.getId());
            d.setTitre(m.getTitre());
            d.setDescription(m.getDescription());
            d.setBudget(m.getBudget());
            d.setDevise(m.getDevise());
            d.setStatut(m.getStatut());
            d.setCategorie(m.getCategorie());
            d.setModaliteTravail(m.getModaliteTravail());
            d.setTypeRemuneration(m.getTypeRemuneration());
            d.setBudgetMin(m.getBudgetMin());
            d.setBudgetMax(m.getBudgetMax());
            d.setTjmJournalier(m.getTjmJournalier());
            d.setGouvernorat(m.getGouvernorat());
            d.setDateLimiteCandidature(m.getDateLimiteCandidature());
            d.setDateDebutSouhaitee(m.getDateDebutSouhaitee());
            d.setChargeHebdoJours(m.getChargeHebdoJours());
            d.setUrgent(m.isUrgent());
            d.setQualiteBrief(m.getQualiteBrief());
            d.setNiveauExperienceMin(m.getNiveauExperienceMin());
            d.setScoreMatching(m.getScoreMatching());
            d.setCandidatsCount(m.getCandidatsCount());
            d.setBadges(m.getBadges());
            d.setDerniereActiviteAt(m.getDerniereActiviteAt());
            d.setDelaiLivraison(m.getDelaiLivraison());
            if (m.getClient() != null) d.setClient(toClientInfoDTO(m.getClient()));
            if (m.getFreelanceSelectionne() != null) d.setFreelance(toFreelanceSummary(m.getFreelanceSelectionne()));

            // Clôture / policy
            d.setClosurePolicy(m.getClosurePolicy());
            d.setPretACloturer(m.getStatut() == Mission.Statut.PRET_A_CLOTURER);
            d.setClosedByClient(m.isClosedByClient());
            d.setClosedByFreelancer(m.isClosedByFreelancer());
            d.setContractTotalAmount(m.getContractTotalAmount());

            // KPI livrables (agrégés)
            var agg = livAggByMission.get(m.getId());
            int total   = agg == null ? 0 : agg.getTotal().intValue();
            int valides = agg == null ? 0 : agg.getValides().intValue();
            int pending = agg == null ? 0 : agg.getEnAttente().intValue();
            d.setLivrablesTotal(total);
            d.setLivrablesValides(valides);
            d.setLivrablesEnAttente(pending);
            d.setProgressPct(total == 0 ? 0 : (int) Math.floor(valides * 100.0 / total));
            d.setLivrableIdEnAttente(null); // optionnel: ajouter une agg dédiée si besoin

            // Tranche due (agrégée)
            Long trancheIdDue = trancheDueByMission.get(m.getId());
            boolean trancheDue = trancheIdDue != null;
            d.setTrancheDue(trancheDue);
            d.setTrancheIdDue(trancheIdDue);

            // CTA principal
            MissionCardDto.NextAction next;
            if (pending > 0) {
                next = MissionCardDto.NextAction.VALIDER_LIVRABLE;
            } else if (trancheDue) {
                next = MissionCardDto.NextAction.PAYER_TRANCHE;
            } else if (m.getFreelanceSelectionne() == null) {
                next = MissionCardDto.NextAction.BOOSTER;
            } else {
                next = MissionCardDto.NextAction.DETAILS;
            }
            d.setNextAction(next);

            return d;
        }).toList();
    }

    private ClientInfoDTO toClientInfoDTO(Utilisateur client) {
        ClientInfoDTO c = new ClientInfoDTO();
        c.setId(client.getId());
        c.setNom(client.getNom());
        c.setPrenom(client.getPrenom());
        c.setPhotoUrl(client.getPhotoProfilUrl());
        c.setVille(client.getLocalisation());
        c.setTypeClient(client.getTypeClient());
        c.setTimezone(client.getTimezone());
        c.setMissionsPubliees(client.getMissionsPubliees());
        c.setNoteDonneeMoy(client.getNoteDonneeMoy());
        c.setFiabilitePaiement(client.getFiabilitePaiement());
        c.setDelaiPaiementMoyenJours(client.getDelaiPaiementMoyenJours());
        c.setEmailVerifie(client.isEmailVerifie());
        c.setTelephoneVerifie(client.isTelephoneVerifie());
        c.setIdentiteVerifiee(client.isIdentiteVerifiee());
        c.setRibVerifie(client.isRibVerifie());
        c.setKycStatut(client.getKycStatut());
        c.setNomEntreprise(client.getNomEntreprise());
        c.setSiteEntreprise(client.getSiteEntreprise());
        c.setDescriptionEntreprise(client.getDescriptionEntreprise());
        c.setBadges(client.getListeBadges());
        return c;
    }

    private FreelanceSummaryDTO toFreelanceSummary(Utilisateur u) {
        FreelanceSummaryDTO f = new FreelanceSummaryDTO();
        f.setId(u.getId());
        f.setNom(u.getNom());
        f.setPrenom(u.getPrenom());
        f.setPhotoUrl(u.getPhotoProfilUrl());
        f.setLocalisation(u.getLocalisation());
        if (u.getTitreProfil() != null) f.setTitreProfil(u.getTitreProfil());
        if (u.getNiveauExperience() != null) f.setNiveauExperience(u.getNiveauExperience().name());
        if (u.getDisponibilite() != null) f.setDisponibilite(u.getDisponibilite().name());
        f.setTarifHoraire(u.getTarifHoraire());
        f.setTarifJournalier(u.getTarifJournalier());
        f.setNoteMoyenne(u.getNoteMoyenne());
        f.setNombreAvis(u.getNombreAvis());
        f.setTimezone(u.getTimezone());
        f.setMobilite(u.getMobilite());
        f.setLinkedinUrl(u.getLinkedinUrl());
        f.setGithubUrl(u.getGithubUrl());
        f.setPreferenceDuree(u.getPreferenceDuree());
        return f;
    }

    /* ------------------------------------------------------------------
       10. Missions d’un freelance sélectionné
       ------------------------------------------------------------------ */
    @Transactional(readOnly = true)
    public List<Mission> getMissionsByFreelance(Long freelanceId) {
        return missionRepository.findAll().stream()
                .filter(m -> m.getFreelanceSelectionne() != null
                        && m.getFreelanceSelectionne().getId().equals(freelanceId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<com.projet.freelencetinder.models.Livrable> listLivrables(Long missionId) {
        return livrableRepository.findByMissionIdOrderByDateEnvoiDesc(missionId);
    }

    /* ------------------------------------------------------------------
       Detail View DTO (assemblage complet pour le front)
       ------------------------------------------------------------------ */
    @Transactional(readOnly = true)
    public com.projet.freelencetinder.dto.MissionDetailViewDTO buildDetailView(Long missionId, Long viewerId) {
        Mission m = getMissionById(missionId);

        var dto = new com.projet.freelencetinder.dto.MissionDetailViewDTO();
        dto.setId(m.getId());
        dto.setTitre(m.getTitre());
        dto.setCategorie(m.getCategorie());
        dto.setStatut(m.getStatut());
        dto.setBudget(m.getBudget());
        dto.setDevise(m.getDevise());
        dto.setDelaiLivraison(m.getDelaiLivraison());
        dto.setLocalisation(m.getLocalisation());
        dto.setUrgent(m.isUrgent());

        dto.setModaliteTravail(m.getModaliteTravail());
        dto.setGouvernorat(m.getGouvernorat());
        dto.setDateDebutSouhaitee(m.getDateDebutSouhaitee());
        dto.setChargeHebdoJours(m.getChargeHebdoJours());
        dto.setDureeEstimeeJours(m.getDureeEstimeeJours());
        dto.setQualiteBrief(m.getQualiteBrief());
        dto.setNiveauExperienceMin(m.getNiveauExperienceMin());
        dto.setBadges(m.getBadges());
        dto.setClientNomComplet(m.getClient() == null ? null :
                (m.getClient().getPrenom() + " " + m.getClient().getNom()).trim());
        dto.setFreelanceNomComplet(m.getFreelanceSelectionne() == null ? null :
                (m.getFreelanceSelectionne().getPrenom() + " " + m.getFreelanceSelectionne().getNom()).trim());

        dto.setClosurePolicy(m.getClosurePolicy());
        dto.setClosedByClient(m.isClosedByClient());
        dto.setClosedByFreelancer(m.isClosedByFreelancer());
        dto.setContractTotalAmount(m.getContractTotalAmount());

        dto.setMediaUrls(m.getMediaUrls());
        dto.setVideoBriefUrl(m.getVideoBriefUrl());

        // Paiements via summary (vérifie aussi les droits d'accès)
        var sum = escrowService.summary(missionId, viewerId);
        var p = new com.projet.freelencetinder.dto.PaymentMiniDTO();
        p.setTotalBrut(sum.getTotalBrut());
        p.setTotalNetFreelance(sum.getTotalNetFreelance());
        p.setPaidTotal(sum.getPaidTotal());
        int pct = (sum.getTotalBrut() == null || sum.getTotalBrut().signum() == 0)
                ? 0
                : sum.getPaidTotal().multiply(java.math.BigDecimal.valueOf(100))
                    .divide(sum.getTotalBrut(), java.math.RoundingMode.DOWN).intValue();
        p.setProgressionPct(pct);

        var minis = new java.util.ArrayList<com.projet.freelencetinder.dto.TrancheMiniDTO>();
        for (var t : sum.getTranches()) {
            var mi = new com.projet.freelencetinder.dto.TrancheMiniDTO();
            mi.setId(t.getId());
            mi.setOrdre(t.getOrdre());
            mi.setTitre(t.getTitre());
            mi.setStatut(t.getStatut());
            mi.setMontantBrut(t.getMontantBrut());
            mi.setRequired(t.isRequired());
            mi.setFinale(t.isFinale());
            mi.setPaymentUrl(t.getPaymeePaymentUrl());
            minis.add(mi);
        }
        p.setTranches(minis);
        dto.setPaiements(p);

        // Livrables lites
        boolean viewerIsClient = (m.getClient() != null && m.getClient().getId().equals(viewerId));
        var lits = new java.util.ArrayList<com.projet.freelencetinder.dto.LivrableLiteDTO>();
        for (var lv : listLivrables(missionId)) {
            var li = new com.projet.freelencetinder.dto.LivrableLiteDTO();
            li.setId(lv.getId());
            li.setTitre(lv.getTitre());
            li.setDescription(lv.getDescription());
            li.setStatus(lv.getStatus());
            li.setDateEnvoi(lv.getDateEnvoi());
            li.setCheminsFichiers(lv.getCheminsFichiers());
            li.setLiensExternes(lv.getLiensExternes());
            boolean enAttente = lv.getStatus() == com.projet.freelencetinder.models.StatusLivrable.EN_ATTENTE;
            li.setCanValidate(viewerIsClient && enAttente);
            li.setCanReject(viewerIsClient && enAttente);
            lits.add(li);
        }
        dto.setLivrables(lits);

        return dto;
    }

    /* ------------------------------------------------------------------
       Detail View côté Freelance (lecture logique)
       ------------------------------------------------------------------ */
    @Transactional(readOnly = true)
    public com.projet.freelencetinder.dto.FreelancerMissionDetailDTO buildFreelancerDetailView(Long missionId, Long viewerId) {
        Mission m = getMissionById(missionId);

        var dto = new com.projet.freelencetinder.dto.FreelancerMissionDetailDTO();
        dto.setId(m.getId());
        dto.setTitre(m.getTitre());
        dto.setDescription(m.getDescription());
        dto.setCategorie(m.getCategorie());
        dto.setStatut(m.getStatut());

        // Exigences & matching
        if (m.getCompetencesRequises() != null) {
            dto.setCompetencesRequises(m.getCompetencesRequises().stream().toList());
        }
        if (m.getCompetencesPriorisees() != null) {
            java.util.Map<String, String> priorisees = new java.util.HashMap<>();
            for (var e : m.getCompetencesPriorisees().entrySet()) {
                priorisees.put(e.getKey(), e.getValue() != null ? e.getValue().name() : null);
            }
            dto.setCompetencesPriorisees(priorisees);
        }
        dto.setLanguesRequises(m.getLanguesRequises());
        dto.setNiveauExperienceMin(m.getNiveauExperienceMin());
        dto.setScoreMatching(m.getScoreMatching());
        dto.setRaisonsMatching(m.getRaisonsMatching());

        // Budget & rémunération
        dto.setBudget(m.getBudget());
        dto.setDevise(m.getDevise());
        dto.setTypeRemuneration(m.getTypeRemuneration());
        dto.setBudgetMin(m.getBudgetMin());
        dto.setBudgetMax(m.getBudgetMax());
        dto.setTjmJournalier(m.getTjmJournalier());

        // Planning / charge
        dto.setDelaiLivraison(m.getDelaiLivraison());
        dto.setDureeEstimeeJours(m.getDureeEstimeeJours());
        dto.setDateLimiteCandidature(m.getDateLimiteCandidature());
        dto.setDateDebutSouhaitee(m.getDateDebutSouhaitee());
        dto.setChargeHebdoJours(m.getChargeHebdoJours());

        // Localisation / modalité
        dto.setLocalisation(m.getLocalisation());
        dto.setGouvernorat(m.getGouvernorat());
        dto.setModaliteTravail(m.getModaliteTravail());

        // Qualité & activité
        dto.setUrgent(m.isUrgent());
        dto.setQualiteBrief(m.getQualiteBrief());
        dto.setDerniereActiviteAt(m.getDerniereActiviteAt());
        dto.setExpired(m.estExpirée());

        // Stats & badges
        dto.setCandidatsCount(m.getCandidatsCount());
        dto.setSwipesRecus(m.getSwipesRecus());
        dto.setLikesRecus(m.getLikesRecus());
        dto.setBadges(m.getBadges());

        // Médias
        dto.setMediaUrls(m.getMediaUrls());
        dto.setVideoBriefUrl(m.getVideoBriefUrl());

        // Client
        if (m.getClient() != null) dto.setClient(toClientInfoDTO(m.getClient()));

        // Contexte viewer freelance
        boolean selectionne = (m.getFreelanceSelectionne() != null
                && viewerId != null
                && m.getFreelanceSelectionne().getId().equals(viewerId));
        dto.setSelectionne(selectionne);
        dto.setCanDeliver(selectionne);

        // Si sélectionné, exposer paiements et livrables
        if (selectionne) {
            var sum = escrowService.summary(missionId, viewerId);
            var p = new com.projet.freelencetinder.dto.PaymentMiniDTO();
            p.setTotalBrut(sum.getTotalBrut());
            p.setTotalNetFreelance(sum.getTotalNetFreelance());
            p.setPaidTotal(sum.getPaidTotal());
            int pct = (sum.getTotalBrut() == null || sum.getTotalBrut().signum() == 0)
                    ? 0
                    : sum.getPaidTotal().multiply(java.math.BigDecimal.valueOf(100))
                        .divide(sum.getTotalBrut(), java.math.RoundingMode.DOWN).intValue();
            p.setProgressionPct(pct);

            var minis = new java.util.ArrayList<com.projet.freelencetinder.dto.TrancheMiniDTO>();
            for (var t : sum.getTranches()) {
                var mi = new com.projet.freelencetinder.dto.TrancheMiniDTO();
                mi.setId(t.getId());
                mi.setOrdre(t.getOrdre());
                mi.setTitre(t.getTitre());
                mi.setStatut(t.getStatut());
                mi.setMontantBrut(t.getMontantBrut());
                mi.setRequired(t.isRequired());
                mi.setFinale(t.isFinale());
                mi.setPaymentUrl(t.getPaymeePaymentUrl());
                minis.add(mi);
            }
            p.setTranches(minis);
            dto.setPaiements(p);

            var lits = new java.util.ArrayList<com.projet.freelencetinder.dto.LivrableLiteDTO>();
            for (var lv : listLivrables(missionId)) {
                var li = new com.projet.freelencetinder.dto.LivrableLiteDTO();
                li.setId(lv.getId());
                li.setTitre(lv.getTitre());
                li.setDescription(lv.getDescription());
                li.setStatus(lv.getStatus());
                li.setDateEnvoi(lv.getDateEnvoi());
                li.setCheminsFichiers(lv.getCheminsFichiers());
                li.setLiensExternes(lv.getLiensExternes());
                // Côté freelance: actions gérées ailleurs; flags d'action non nécessaires ici
                li.setCanValidate(false);
                li.setCanReject(false);
                lits.add(li);
            }
            dto.setLivrables(lits);
        }

        return dto;
    }

    /* ------------------------------------------------------------------
       Utilitaires potentiellement réutilisables
       ------------------------------------------------------------------ */
    @Transactional(readOnly = true)
    public Utilisateur getUtilisateurOrThrow(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable (id=" + id + ")"));
    }

    /**
     * Assigne la mission au freelance spécifié et passe la mission en statut EN_COURS.
     */
    @Transactional
    public Mission assignMissionToFreelancer(Long missionId, Long freelanceId) {
        Mission mission = getMissionById(missionId);

        if (mission.getStatut() != Statut.EN_ATTENTE) {
            throw new IllegalStateException(
              "Impossible d’assigner la mission (statut=" + mission.getStatut() + ")");
        }

        Utilisateur freelance = getUtilisateurOrThrow(freelanceId);
        mission.setFreelanceSelectionne(freelance);
        mission.setStatut(Statut.EN_COURS);
        mission.setVerrouillee(true); // AJOUT : verrouiller dès l’assignation

        return missionRepository.save(mission);
    }

    /* ======================================================================
       AJOUTS : Helpers de gestion fine (verrouillage, expiration, médias, skills)
       ====================================================================== */

    /** Verrouille explicitement une mission (non swipable). */
    @Transactional
    public Mission lockMission(Long missionId) {
        Mission m = getMissionById(missionId);
        m.setVerrouillee(true);
        return missionRepository.save(m);
    }

    /** Déverrouille une mission si elle est encore EN_ATTENTE. */
    @Transactional
    public Mission unlockMission(Long missionId) {
        Mission m = getMissionById(missionId);
        if (m.getStatut() != Statut.EN_ATTENTE) {
            throw new IllegalStateException("Déverrouillage interdit si la mission n’est pas EN_ATTENTE");
        }
        m.setVerrouillee(false);
        return missionRepository.save(m);
    }

    /** Force l’expiration si la date limite est passée. */
    @Transactional
    public Mission expireIfDeadlinePassed(Long missionId) {
        Mission m = getMissionById(missionId);
        if (m.estExpirée() && (m.getStatut() == Statut.EN_ATTENTE)) {
            m.setStatut(Statut.EXPIREE);
            m.setVerrouillee(true);
            return missionRepository.save(m);
        }
        return m;
    }

    /** Met à jour uniquement la localisation (ville/gouvernorat libre dans `localisation`). */
    @Transactional
    public Mission updateLocalisation(Long missionId, String localisation) {
        Mission m = getMissionById(missionId);
        if (StringUtils.hasText(localisation)) {
            m.setLocalisation(localisation.trim());
            return missionRepository.save(m);
        }
        return m;
    }

    /** Ajoute une compétence requise (si absente). */
    @Transactional
    public Mission addRequiredSkill(Long missionId, String skill) {
        Mission m = getMissionById(missionId);
        if (StringUtils.hasText(skill)) {
            m.getCompetencesRequises().add(skill.trim());
            return missionRepository.save(m);
        }
        return m;
    }

    /** Supprime une compétence requise. */
    @Transactional
    public Mission removeRequiredSkill(Long missionId, String skill) {
        Mission m = getMissionById(missionId);
        if (StringUtils.hasText(skill)) {
            m.getCompetencesRequises().remove(skill.trim());
            return missionRepository.save(m);
        }
        return m;
    }

    /** Append d’un média (URL) sans écraser la liste. */
    @Transactional
    public Mission addMediaUrl(Long missionId, String url) {
        Mission m = getMissionById(missionId);
        if (StringUtils.hasText(url)) {
            m.getMediaUrls().add(url.trim());
            return missionRepository.save(m);
        }
        return m;
    }

    /** Retrait d’un média (URL). */
    @Transactional
    public Mission removeMediaUrl(Long missionId, String url) {
        Mission m = getMissionById(missionId);
        if (StringUtils.hasText(url) && m.getMediaUrls().remove(url.trim())) {
            return missionRepository.save(m);
        }
        return m;
    }

    /** Changement de modalité de travail (DN/PRESENTIEL/HYBRIDE/NON_SPECIFIE). */
    @Transactional
    public Mission changeModalite(Long missionId, ModaliteTravail modalite) {
        Mission m = getMissionById(missionId);
        if (m.getStatut() != Statut.EN_ATTENTE) {
            throw new IllegalStateException("Impossible de changer la modalité si la mission n’est pas EN_ATTENTE");
        }
        m.setModaliteTravail(modalite != null ? modalite : ModaliteTravail.NON_SPECIFIE);
        return missionRepository.save(m);
    }

    /* ======================================================================
       Validation centralisée (AJOUT)
       ====================================================================== */
    private void validateMission(Mission m, boolean isNew) {
        if (!StringUtils.hasText(m.getTitre()))
            throw new IllegalArgumentException("Titre requis.");
        if (!StringUtils.hasText(m.getDescription()))
            throw new IllegalArgumentException("Description requise.");

        if (m.getBudget() == null || m.getBudget().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Budget invalide (>= 0).");

        if (!StringUtils.hasText(m.getDevise()))
            throw new IllegalArgumentException("Devise requise (ex: TND).");

        if (m.getCategorie() == null)
            throw new IllegalArgumentException("Catégorie requise.");

        // Deadline cohérente : pas avant aujourd’hui
        if (m.getDateLimiteCandidature() != null && m.getDateLimiteCandidature().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La date limite de candidature ne peut pas être dans le passé.");
        }

        // Délai de livraison cohérent : s’il existe, pas avant demain
        if (m.getDelaiLivraison() != null && !m.getDelaiLivraison().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Le délai de livraison doit être ultérieur à aujourd’hui.");
        }

        // Modalité/Statut par défaut si absent (déjà forcé en création)
        if (m.getModaliteTravail() == null) m.setModaliteTravail(ModaliteTravail.NON_SPECIFIE);
        if (m.getStatut() == null) m.setStatut(Statut.EN_ATTENTE);

        // Localisation : facultative mais on nettoie
        if (m.getLocalisation() != null) m.setLocalisation(m.getLocalisation().trim());
    }
}

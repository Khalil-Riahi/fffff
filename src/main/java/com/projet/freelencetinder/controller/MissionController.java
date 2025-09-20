package com.projet.freelencetinder.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.projet.freelencetinder.dto.ClientInfoDTO;
import com.projet.freelencetinder.dto.FreelanceSummaryDTO;
import com.projet.freelencetinder.dto.MissionCardDto;
import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.models.Mission.ModaliteTravail;
import com.projet.freelencetinder.models.Utilisateur; // pour accéder au client de la mission
import com.projet.freelencetinder.servcie.MissionService;
import com.projet.freelencetinder.servcie.EscrowService;
import java.math.BigDecimal;

import jakarta.persistence.EntityNotFoundException;

/**
 * Contrôleur REST focalisé sur la gestion CRUD des missions.
 * Toute la logique de swipe / matching / recommandations est déplacée dans SwipeController.
 */
@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;
    private final EscrowService escrow;

    @Autowired
    public MissionController(MissionService missionService, EscrowService escrow) {
        this.missionService = missionService;
        this.escrow = escrow;
    }

    /* ================================================================
       1. Création d’une mission
       ================================================================ */
    @PostMapping
    public ResponseEntity<Mission> createMission(@RequestBody Mission mission) {
        Mission created = missionService.createMission(mission);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /* ================================================================
       2. Liste brute (prototype – à paginer plus tard)
       ================================================================ */
    @GetMapping
    public ResponseEntity<List<Mission>> getAllMissions() {
        return ResponseEntity.ok(missionService.getAllMissions());
    }

    /* ================================================================
       3. Détails mission
       ================================================================ */
    @GetMapping("/{id}")
    public ResponseEntity<Mission> getMissionById(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.getMissionById(id));
    }

    /* ================================================================
       4. Mise à jour mission (statut EN_ATTENTE)
       ================================================================ */
    @PutMapping("/{id}")
    public ResponseEntity<Mission> updateMission(@PathVariable Long id,
                                                 @RequestBody Mission mission) {
        return ResponseEntity.ok(missionService.updateMission(id, mission));
    }

    /* ================================================================
       5. Suppression mission
       ================================================================ */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMission(@PathVariable Long id) {
        missionService.deleteMission(id);
        return ResponseEntity.noContent().build();
    }

    /* ================================================================
       6. Missions d’un client → MissionCardDto
       ================================================================ */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<MissionCardDto>> getMissionsByClient(@PathVariable Long clientId) {
        List<MissionCardDto> dtos = missionService.getMissionCardsByClient(clientId);
        return ResponseEntity.ok(dtos);
    }

    /* Variante sécurisée: utilise l'utilisateur authentifié (ou header local) */
    @GetMapping("/client/me")
    public ResponseEntity<List<MissionCardDto>> getMyMissions(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long clientId = resolveUserId(headerUserId);
        return ResponseEntity.ok(missionService.getMissionCardsByClient(clientId));
    }

    private Long resolveUserId(Long headerUserId) {
        if (headerUserId != null) return headerUserId;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || "anonymousUser".equalsIgnoreCase(auth.getName())) {
            throw new IllegalArgumentException("Non authentifié. En local, passez X-User-Id dans les headers.");
        }
        // Si nécessaire, mappe email -> id via repository; ici, on attend X-User-Id en local
        throw new IllegalArgumentException("Résolution d'utilisateur non configurée sans X-User-Id.");
    }

    private MissionCardDto toCardDto(Mission m) {
        MissionCardDto d = new MissionCardDto();
        d.setId(m.getId());
        d.setTitre(m.getTitre());
        d.setDescription(m.getDescription());
        d.setBudget(m.getBudget());
        d.setDevise(m.getDevise());
        d.setStatut(m.getStatut());

        // Champs supplémentaires si présents sur l’entité Mission
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

        d.setUrgent(m.isUrgent()); // si le champ est Boolean, remplace par: Boolean.TRUE.equals(m.getUrgent())
        d.setQualiteBrief(m.getQualiteBrief());
        d.setNiveauExperienceMin(m.getNiveauExperienceMin());

        d.setScoreMatching(m.getScoreMatching());
        //d.setMatchReasons(m.getMatchReasons());
        d.setCandidatsCount(m.getCandidatsCount());
        d.setBadges(m.getBadges());

        d.setDerniereActiviteAt(m.getDerniereActiviteAt());

        // ----- Mapping du client vers ClientInfoDTO -----
        if (m.getClient() != null) {
            d.setClient(toClientInfoDTO(m.getClient()));
        }

        // ----- Mapping du freelance sélectionné vers FreelanceSummaryDTO -----
        if (m.getFreelanceSelectionne() != null) {
            var u = m.getFreelanceSelectionne();
            FreelanceSummaryDTO f = new FreelanceSummaryDTO();
            f.setId(u.getId());
            f.setNom(u.getNom());
            f.setPrenom(u.getPrenom());
            f.setPhotoUrl(u.getPhotoProfilUrl());
            f.setLocalisation(u.getLocalisation());

            if (u.getTitreProfil() != null) f.setTitreProfil(u.getTitreProfil());
            if (u.getNiveauExperience() != null) f.setNiveauExperience(u.getNiveauExperience().name());
            if (u.getDisponibilite() != null)   f.setDisponibilite(u.getDisponibilite().name());

            f.setTarifHoraire(u.getTarifHoraire());
            f.setTarifJournalier(u.getTarifJournalier());
            f.setNoteMoyenne(u.getNoteMoyenne());
            f.setNombreAvis(u.getNombreAvis());
            f.setTimezone(u.getTimezone());
            f.setMobilite(u.getMobilite());
            f.setLinkedinUrl(u.getLinkedinUrl());
            f.setGithubUrl(u.getGithubUrl());
            f.setPreferenceDuree(u.getPreferenceDuree());
            d.setFreelance(f);
        }

        return d;
    }

    private ClientInfoDTO toClientInfoDTO(Utilisateur client) {
        ClientInfoDTO c = new ClientInfoDTO();
        c.setId(client.getId());
        c.setNom(client.getNom());
        c.setPrenom(client.getPrenom());
        c.setPhotoUrl(client.getPhotoProfilUrl());

        // localisation “ville” côté client : on réutilise la localisation du profil si renseignée
        c.setVille(client.getLocalisation());

        c.setTypeClient(client.getTypeClient());
        c.setTimezone(client.getTimezone());

        // Confiance / réputation
        c.setMissionsPubliees(client.getMissionsPubliees());
        c.setNoteDonneeMoy(client.getNoteDonneeMoy());
        c.setFiabilitePaiement(client.getFiabilitePaiement());
        c.setDelaiPaiementMoyenJours(client.getDelaiPaiementMoyenJours());

        // Vérifs / KYC
        c.setEmailVerifie(client.isEmailVerifie());
        c.setTelephoneVerifie(client.isTelephoneVerifie());
        c.setIdentiteVerifiee(client.isIdentiteVerifiee());
        c.setRibVerifie(client.isRibVerifie());
        c.setKycStatut(client.getKycStatut());

        // Entreprise
        c.setNomEntreprise(client.getNomEntreprise());
        c.setSiteEntreprise(client.getSiteEntreprise());
        c.setDescriptionEntreprise(client.getDescriptionEntreprise());

        // Badges
        c.setBadges(client.getListeBadges());

        // Gouvernorat : si tu stockes un gouvernorat côté client, mappe-le ici.
        // Sinon, laisse null (le gouvernorat de la mission figure déjà sur la carte mission).
        // c.setGouvernorat(client.getGouvernoratClient()); // <- décommente si présent dans ton modèle

        return c;
    }

    /* ================================================================
       7. Missions où un freelance est sélectionné
       ================================================================ */
    @GetMapping("/freelance/{freelanceId}")
    public ResponseEntity<List<Mission>> getMissionsByFreelance(@PathVariable Long freelanceId) {
        return ResponseEntity.ok(missionService.getMissionsByFreelance(freelanceId));
    }

    /* ================================================================
       8. Assignation manuelle (override)
       ================================================================ */
    @PutMapping("/{missionId}/assign/{freelanceId}")
    public ResponseEntity<Mission> assignMission(
            @PathVariable Long missionId,
            @PathVariable Long freelanceId) {
        Mission assigned = missionService.assignMissionToFreelancer(missionId, freelanceId);
        return ResponseEntity.ok(assigned);
    }

    /* ====================== AJOUTS : gestion fine ====================== */

    /** Verrouiller explicitement une mission (non swipable). */
    @PostMapping("/{missionId}/lock")
    public ResponseEntity<Mission> lockMission(@PathVariable Long missionId) {
        return ResponseEntity.ok(missionService.lockMission(missionId));
    }

    /** Déverrouiller une mission (si EN_ATTENTE). */
    @PostMapping("/{missionId}/unlock")
    public ResponseEntity<Mission> unlockMission(@PathVariable Long missionId) {
        return ResponseEntity.ok(missionService.unlockMission(missionId));
    }

    /** Forcer l’expiration si date limite passée (utile batch/cron). */
    @PostMapping("/{missionId}/expire-if-deadline-passed")
    public ResponseEntity<Mission> expireIfDeadlinePassed(@PathVariable Long missionId) {
        return ResponseEntity.ok(missionService.expireIfDeadlinePassed(missionId));
    }

    /** Patch localisation (ville/gouvernorat en clair). */
    @PatchMapping("/{missionId}/localisation")
    public ResponseEntity<Mission> updateLocalisation(@PathVariable Long missionId,
                                                      @RequestParam String value) {
        return ResponseEntity.ok(missionService.updateLocalisation(missionId, value));
    }

    /** Ajouter une compétence requise sans écraser la liste. */
    @PostMapping("/{missionId}/skills")
    public ResponseEntity<Mission> addRequiredSkill(@PathVariable Long missionId,
                                                    @RequestParam String skill) {
        return ResponseEntity.ok(missionService.addRequiredSkill(missionId, skill));
    }

    /** Supprimer une compétence requise. */
    @DeleteMapping("/{missionId}/skills")
    public ResponseEntity<Mission> removeRequiredSkill(@PathVariable Long missionId,
                                                       @RequestParam String skill) {
        return ResponseEntity.ok(missionService.removeRequiredSkill(missionId, skill));
    }

    /** Ajouter une URL média (images/docs/brief). */
    @PostMapping("/{missionId}/media")
    public ResponseEntity<Mission> addMediaUrl(@PathVariable Long missionId,
                                               @RequestParam String url) {
        return ResponseEntity.ok(missionService.addMediaUrl(missionId, url));
    }

    /** Supprimer une URL média. */
    @DeleteMapping("/{missionId}/media")
    public ResponseEntity<Mission> removeMediaUrl(@PathVariable Long missionId,
                                                  @RequestParam String url) {
        return ResponseEntity.ok(missionService.removeMediaUrl(missionId, url));
    }

    /** Changer modalité de travail (DISTANCIEL/PRESENTIEL/HYBRIDE/NON_SPECIFIE). */
    @PatchMapping("/{missionId}/modalite")
    public ResponseEntity<Mission> changeModalite(@PathVariable Long missionId,
                                                  @RequestParam ModaliteTravail value) {
        return ResponseEntity.ok(missionService.changeModalite(missionId, value));
    }

    @GetMapping("/{id}/detail-view")
    public ResponseEntity<com.projet.freelencetinder.dto.MissionDetailViewDTO> getMissionDetailView(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long viewerId = (headerUserId != null) ? headerUserId : null;
        return ResponseEntity.ok(missionService.buildDetailView(id, viewerId));
    }

    @GetMapping("/{id}/freelancer-view")
    public ResponseEntity<com.projet.freelencetinder.dto.FreelancerMissionDetailDTO> getFreelancerMissionDetailView(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long viewerId = (headerUserId != null) ? headerUserId : null;
        return ResponseEntity.ok(missionService.buildFreelancerDetailView(id, viewerId));
    }

    

    /* ====================== AJOUTS : politique de clôture ====================== */
    @PatchMapping("/{missionId}/closure-policy")
    public ResponseEntity<Mission> updateClosurePolicy(@PathVariable Long missionId,
                                                       @RequestParam Mission.ClosurePolicy value,
                                                       @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long clientId = resolveUserId(headerUserId);
        return ResponseEntity.ok(escrow.updateMissionClosurePolicy(missionId, clientId, value));
    }

    @PatchMapping("/{missionId}/contract-total")
    public ResponseEntity<Mission> updateContractTotal(@PathVariable Long missionId,
                                                       @RequestParam BigDecimal amount,
                                                       @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long clientId = resolveUserId(headerUserId);
        return ResponseEntity.ok(escrow.updateContractTotalAmount(missionId, clientId, amount));
    }

    @PostMapping("/{missionId}/confirm-close/client")
    public ResponseEntity<Mission> confirmCloseClient(@PathVariable Long missionId,
                                                      @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long clientId = resolveUserId(headerUserId);
        return ResponseEntity.ok(escrow.confirmCloseByClient(missionId, clientId));
    }

    @PostMapping("/{missionId}/confirm-close/freelancer")
    public ResponseEntity<Mission> confirmCloseFreelancer(@PathVariable Long missionId,
                                                          @RequestHeader("X-Freelancer-Id") Long freelancerId) {
        return ResponseEntity.ok(escrow.confirmCloseByFreelancer(missionId, freelancerId));
    }

    /* ================================================================
       Gestion d’erreurs locale (optionnel – sinon @ControllerAdvice global)
       ================================================================ */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}

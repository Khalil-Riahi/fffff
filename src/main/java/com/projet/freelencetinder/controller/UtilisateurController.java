package com.projet.freelencetinder.controller;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.models.Mission.Categorie;
import com.projet.freelencetinder.models.Mission.Gouvernorat;
import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.models.Utilisateur.Disponibilite;
import com.projet.freelencetinder.models.Utilisateur.Langue;
import com.projet.freelencetinder.models.Utilisateur.NiveauExperience;
import com.projet.freelencetinder.models.Utilisateur.StatutKyc;
import com.projet.freelencetinder.models.Utilisateur.TypeUtilisateur;
import com.projet.freelencetinder.models.Utilisateur.Mobilite;
import com.projet.freelencetinder.models.Utilisateur.PreferenceDuree;
import com.projet.freelencetinder.models.Utilisateur.TypeClient;

import com.projet.freelencetinder.servcie.UtilisateurService;

import com.projet.freelencetinder.dto.FreelanceSummaryDTO;
import com.projet.freelencetinder.dto.FreelanceSummaryAssembler;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @Autowired
    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    /* 1. Création (usage admin/tests) */
    @PostMapping
    public ResponseEntity<Utilisateur> createUtilisateur(@RequestBody Utilisateur utilisateur) {
        Utilisateur created = utilisateurService.createUtilisateur(utilisateur);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /* 2. Listing + filtres simples (retourne des entités Utilisateur) */
    @GetMapping
    public ResponseEntity<List<Utilisateur>> getAllUtilisateurs(
            @RequestParam(required = false) TypeUtilisateur type,
            @RequestParam(required = false) String localisation,
            @RequestParam(required = false) String competences,   // CSV
            @RequestParam(required = false) Double tarifMin,
            @RequestParam(required = false) Double tarifMax,
            @RequestParam(required = false) Double tarifJourMin,
            @RequestParam(required = false) Double tarifJourMax,
            @RequestParam(required = false) Disponibilite dispo,
            @RequestParam(required = false) NiveauExperience niveau,
            @RequestParam(required = false) Langue langue,
            @RequestParam(required = false) List<Categorie> categories,

            /* AJOUTS existants */
            @RequestParam(required = false) Double noteMin,
            @RequestParam(required = false) Integer projetsTerminesMin,

            /* NOUVEAUX FILTRES */
            @RequestParam(required = false) Gouvernorat gouvernorat,
            @RequestParam(required = false) Mobilite mobilite,
            @RequestParam(required = false) String timezone,
            @RequestParam(required = false) PreferenceDuree preferenceDuree,
            @RequestParam(required = false) TypeClient typeClient,
            @RequestParam(required = false) Boolean estActif,

            // Vérifications / KYC
            @RequestParam(required = false) Boolean emailVerifie,
            @RequestParam(required = false) Boolean telephoneVerifie,
            @RequestParam(required = false) Boolean identiteVerifiee,
            @RequestParam(required = false) Boolean ribVerifie,
            @RequestParam(required = false) StatutKyc kycStatut,

            // Réputation
            @RequestParam(required = false) Integer nombreAvisMin
    ) {

        List<Utilisateur> liste = utilisateurService.getAllUtilisateurs();

        List<String> skillsFilter = (competences == null || competences.isBlank())
                ? Collections.emptyList()
                : Arrays.stream(competences.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());

        List<Utilisateur> filtered = liste.stream()
            .filter(u -> type == null || u.getTypeUtilisateur() == type)
            .filter(u -> localisation == null ||
                    (u.getLocalisation() != null &&
                     u.getLocalisation().toLowerCase().contains(localisation.toLowerCase())))
            .filter(u -> skillsFilter.isEmpty()
                    || (u.getCompetences() != null && u.getCompetences().containsAll(skillsFilter)))
            .filter(u -> categories == null || categories.isEmpty() ||
                    (u.getCategories() != null &&
                        u.getCategories().stream().anyMatch(categories::contains)))
            .filter(u -> tarifMin == null ||
                    (u.getTarifHoraire() != null && u.getTarifHoraire() >= tarifMin))
            .filter(u -> tarifMax == null ||
                    (u.getTarifHoraire() != null && u.getTarifHoraire() <= tarifMax))
            .filter(u -> tarifJourMin == null ||
                    (u.getTarifJournalier() != null && u.getTarifJournalier() >= tarifJourMin))
            .filter(u -> tarifJourMax == null ||
                    (u.getTarifJournalier() != null && u.getTarifJournalier() <= tarifJourMax))
            .filter(u -> dispo  == null || u.getDisponibilite()    == dispo)
            .filter(u -> niveau == null || u.getNiveauExperience() == niveau)
            .filter(u -> langue == null || u.getLanguePref()       == langue)

            /* AJOUTS existants */
            .filter(u -> noteMin == null || (u.getNoteMoyenne() != null && u.getNoteMoyenne() >= noteMin))
            .filter(u -> projetsTerminesMin == null || (u.getProjetsTermines() != null && u.getProjetsTermines() >= projetsTerminesMin))

            /* NOUVEAUX FILTRES */
            .filter(u -> gouvernorat == null || u.getGouvernorat() == gouvernorat)
            .filter(u -> mobilite == null || u.getMobilite() == mobilite)
            .filter(u -> timezone == null ||
                    (u.getTimezone() != null && u.getTimezone().toLowerCase().contains(timezone.toLowerCase())))
            .filter(u -> preferenceDuree == null || u.getPreferenceDuree() == preferenceDuree)
            .filter(u -> typeClient == null || u.getTypeClient() == typeClient)
            .filter(u -> estActif == null || u.isEstActif() == estActif)

            // Vérifs / KYC
            .filter(u -> emailVerifie == null || u.isEmailVerifie() == emailVerifie)
            .filter(u -> telephoneVerifie == null || u.isTelephoneVerifie() == telephoneVerifie)
            .filter(u -> identiteVerifiee == null || u.isIdentiteVerifiee() == identiteVerifiee)
            .filter(u -> ribVerifie == null || u.isRibVerifie() == ribVerifie)
            .filter(u -> kycStatut == null || u.getKycStatut() == kycStatut)

            // Réputation
            .filter(u -> nombreAvisMin == null || (u.getNombreAvis() != null && u.getNombreAvis() >= nombreAvisMin))

            .collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
    }

    /* 3. Lire un utilisateur par ID */
    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> getUtilisateurById(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.getUtilisateurById(id));
    }

    /* 4. Mise à jour complète (PUT) */
    @PutMapping("/{id}")
    public ResponseEntity<Utilisateur> updateUtilisateur(@PathVariable Long id,
                                                         @RequestBody Utilisateur utilisateur) {
        return ResponseEntity.ok(utilisateurService.updateUtilisateur(id, utilisateur));
    }

    /* 5. Suppression */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUtilisateur(@PathVariable Long id) {
        utilisateurService.deleteUtilisateur(id);
        return ResponseEntity.noContent().build();
    }

    /* 6. Profil connecté (via SecurityContext) */
    @GetMapping("/me")
    public ResponseEntity<Utilisateur> getCurrentUser(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Utilisateur u = utilisateurService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(u);
    }

    /* 7. Patch profil (freelance) */
    @PatchMapping("/{id}/profil")
    public ResponseEntity<Utilisateur> patchProfil(@PathVariable Long id,
                                                   @RequestBody PatchProfilRequest req) {
        Utilisateur updated = utilisateurService.patchProfil(
                id,
                req.getBio(),
                req.getLocalisation(),
                req.getCompetences(),
                req.getTarifHoraire(),
                req.getTarifJournalier(),
                req.getCategories(),
                req.getPhotoProfilUrl()
        );
        return ResponseEntity.ok(updated);
    }

    /* 8. Activation / désactivation */
    @PostMapping("/{id}/activation")
    public ResponseEntity<Void> setActivation(@PathVariable Long id,
                                              @RequestParam boolean actif) {
        utilisateurService.setActive(id, actif);
        return ResponseEntity.ok().build();
    }

    /* 9. Push tokens (add / remove) */
    @PostMapping("/{id}/push-tokens")
    public ResponseEntity<Void> addPushToken(@PathVariable Long id,
                                             @RequestParam @NotBlank String token) {
        utilisateurService.addPushToken(id, token);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/push-tokens")
    public ResponseEntity<Void> removePushToken(@PathVariable Long id,
                                                @RequestParam @NotBlank String token) {
        utilisateurService.removePushToken(id, token);
        return ResponseEntity.noContent().build();
    }

    /* 10. Incrément direct des compteurs */
    @PostMapping("/{id}/counters/swipe")
    public ResponseEntity<Void> incSwipe(@PathVariable Long id) {
        utilisateurService.incrementSwipe(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/counters/like")
    public ResponseEntity<Void> incLike(@PathVariable Long id) {
        utilisateurService.incrementLikeRecu(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/counters/match")
    public ResponseEntity<Void> incMatch(@PathVariable Long id) {
        utilisateurService.incrementMatch(id);
        return ResponseEntity.ok().build();
    }

    /* 11. Enum metadata */
    @GetMapping("/enums")
    public ResponseEntity<Map<String, Object>> getEnums() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("types", TypeUtilisateur.values());
        map.put("disponibilites", Disponibilite.values());
        map.put("niveauxExperience", NiveauExperience.values());
        map.put("langues", Langue.values());
        map.put("categoriesMission", Categorie.values());

        // AJOUTS
        map.put("gouvernorats", Gouvernorat.values());
        map.put("mobilites", Mobilite.values());
        map.put("preferencesDuree", PreferenceDuree.values());
        map.put("typesClient", TypeClient.values());
        map.put("statutsKyc", StatutKyc.values());
        return ResponseEntity.ok(map);
    }

    /* 12. Vérifications & KYC */
    @PostMapping("/{id}/verify/email")
    public ResponseEntity<Void> verifyEmail(@PathVariable Long id) {
        utilisateurService.verifyEmail(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/verify/phone")
    public ResponseEntity<Void> verifyPhone(@PathVariable Long id) {
        utilisateurService.verifyPhone(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/kyc")
    public ResponseEntity<Void> setKycStatus(@PathVariable Long id,
                                             @RequestParam StatutKyc statut) {
        utilisateurService.setKycStatus(id, statut);
        return ResponseEntity.ok().build();
    }

    /* 13. Gamification : super-likes */
    @PostMapping("/{id}/superlikes/consume")
    public ResponseEntity<Void> consumeSuperLike(@PathVariable Long id) {
        utilisateurService.consumeSuperLike(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/superlikes/set")
    public ResponseEntity<Void> setDailySuperlikes(@PathVariable Long id,
                                                   @RequestParam int count) {
        utilisateurService.setDailySuperlikes(id, count);
        return ResponseEntity.ok().build();
    }

    /* ===================== AJOUTS: ENDPOINTS SUMMARY ===================== */

    /** Liste simple de freelances convertis en FreelanceSummaryDTO (sans filtres avancés). */
    @GetMapping("/freelances/summary")
    public ResponseEntity<List<FreelanceSummaryDTO>> listFreelanceSummaries() {
        List<FreelanceSummaryDTO> dtos = utilisateurService.getAllUtilisateurs().stream()
                .filter(u -> u.getTypeUtilisateur() == TypeUtilisateur.FREELANCE)
                .map(FreelanceSummaryAssembler::toSummary)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /** Détail résumé pour un freelance (404 si ce n'est pas un freelance). */
    @GetMapping("/{id}/summary")
    public ResponseEntity<FreelanceSummaryDTO> getFreelanceSummary(@PathVariable Long id) {
        Utilisateur u = utilisateurService.getUtilisateurById(id);
        if (u.getTypeUtilisateur() != TypeUtilisateur.FREELANCE) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(FreelanceSummaryAssembler.toSummary(u));
    }

    /* ===================== Gestion locale des exceptions ===================== */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /* DTO interne pour patch du profil */
    public static class PatchProfilRequest {
        private String bio;
        private String localisation;
        private Set<Mission.Categorie> categories;
        private Set<String> competences;
        private Double tarifHoraire;
        private Double tarifJournalier;
        private String photoProfilUrl;

        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }

        public String getLocalisation() { return localisation; }
        public void setLocalisation(String localisation) { this.localisation = localisation; }

        public Set<String> getCompetences() { return competences; }
        public void setCompetences(Set<String> competences) { this.competences = competences; }

        public Double getTarifHoraire() { return tarifHoraire; }
        public void setTarifHoraire(Double tarifHoraire) { this.tarifHoraire = tarifHoraire; }

        public Double getTarifJournalier() { return tarifJournalier; }
        public void setTarifJournalier(Double tarifJournalier) { this.tarifJournalier = tarifJournalier; }

        public Set<Mission.Categorie> getCategories() { return categories; }
        public void setCategories(Set<Mission.Categorie> categories) { this.categories = categories; }

        public String getPhotoProfilUrl() { return photoProfilUrl; }
        public void setPhotoProfilUrl(String photoProfilUrl) { this.photoProfilUrl = photoProfilUrl; }
    }
}

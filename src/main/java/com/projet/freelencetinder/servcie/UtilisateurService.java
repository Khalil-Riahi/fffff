package com.projet.freelencetinder.servcie;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.models.Utilisateur.TypeUtilisateur;
import com.projet.freelencetinder.models.Utilisateur.*; // enums internes
import com.projet.freelencetinder.repository.UtilisateurRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final com.projet.freelencetinder.servcie.CompetenceService competenceService;
    private static final Logger log = LoggerFactory.getLogger(UtilisateurService.class);

    /* Regex simples */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8,15}$");
    private static final Pattern URL_PATTERN   = Pattern.compile("^(https?://)[^\\s]+$");

    /* AJOUTS (gamification / défauts Tunisie) */
    private static final String DEFAULT_TN_TIMEZONE = "Africa/Tunis";
    private static final int    DEFAULT_DAILY_SUPERLIKES = 3;

    @Autowired
    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              com.projet.freelencetinder.servcie.CompetenceService competenceService) {
        this.utilisateurRepository = utilisateurRepository;
        this.competenceService = competenceService;
    }

    /* =========================================================
       1. Création
       ========================================================= */
    @Transactional
    public Utilisateur createUtilisateur(Utilisateur utilisateur) {
        validateUtilisateur(utilisateur, true);

        utilisateur.setDateCreation(LocalDateTime.now());
        utilisateur.setDerniereMiseAJour(LocalDateTime.now());
        utilisateur.setEstActif(true);

        // Initialisations sûres si null
        if (utilisateur.getSoldeEscrow() == null) utilisateur.setSoldeEscrow(BigDecimal.ZERO);
        if (utilisateur.getNombreSwipes() == null) utilisateur.setNombreSwipes(0);
        if (utilisateur.getLikesRecus() == null) utilisateur.setLikesRecus(0);
        if (utilisateur.getMatchesObtenus() == null) utilisateur.setMatchesObtenus(0);
        if (utilisateur.getNombreAvis() == null) utilisateur.setNombreAvis(0);

        // Normalisation URLs pro (trim + validation simple)
        utilisateur.setLinkedinUrl(normalizeUrl(utilisateur.getLinkedinUrl()));
        utilisateur.setGithubUrl(normalizeUrl(utilisateur.getGithubUrl()));

        normalizeCollections(utilisateur);

        // Normalisation douce des compétences
        if (utilisateur.getCompetences() != null && !utilisateur.getCompetences().isEmpty()) {
            Set<String> normalized = competenceService.normalizeSet(utilisateur.getCompetences());
            if (!normalized.equals(utilisateur.getCompetences())) {
                log.info("[Utilisateur#create] Compétences normalisées: {} -> {}", utilisateur.getCompetences(), normalized);
            }
            utilisateur.setCompetences(normalized);
        }
        applyTunisiaDefaults(utilisateur);
        sanitizeNumericRanges(utilisateur);

        return utilisateurRepository.save(utilisateur);
    }

    /* =========================================================
       2. Lecture
       ========================================================= */
    @Transactional(readOnly = true)
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Utilisateur getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable avec l'id " + id));
    }

    @Transactional(readOnly = true)
    public Utilisateur getByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable pour " + email));
    }

    /* =========================================================
       3. Mise à jour (remplacement “contrôlé”)
       ========================================================= */
    @Transactional
    public Utilisateur updateUtilisateur(Long id, Utilisateur payload) {
        Utilisateur existing = getUtilisateurById(id);

        // On valide ce qui est modifiable
        validateUtilisateur(payload, false);

        // Champs communs
        existing.setNom(payload.getNom());
        existing.setPrenom(payload.getPrenom());

        // Autoriser le changement d'email *si* différent et non pris
        if (!existing.getEmail().equalsIgnoreCase(payload.getEmail())) {
            if (utilisateurRepository.existsByEmail(payload.getEmail()))
                throw new IllegalArgumentException("Nouvel email déjà utilisé.");
            existing.setEmail(payload.getEmail());
        }

        // Mot de passe : si hash plausible
        if (StringUtils.hasText(payload.getMotDePasse()) &&
            payload.getMotDePasse().length() >= 60) {
            existing.setMotDePasse(payload.getMotDePasse());
        }

        existing.setNumeroTelephone(payload.getNumeroTelephone());
        existing.setPhotoProfilUrl(payload.getPhotoProfilUrl());
        existing.setLanguePref(payload.getLanguePref());

        // Rôle : verrouillé ici
        if (existing.getTypeUtilisateur() != payload.getTypeUtilisateur()) {
            throw new IllegalStateException("Changement de type utilisateur non autorisé.");
        }

        existing.setDateDerniereConnexion(payload.getDateDerniereConnexion());
        existing.setEstActif(payload.isEstActif());

        // Solde escrow (ne jamais laisser null)
        if (payload.getSoldeEscrow() != null) {
            existing.setSoldeEscrow(payload.getSoldeEscrow());
        }

        // Push tokens (merge contrôlé)
        if (payload.getPushTokens() != null) {
            existing.getPushTokens().clear();
            existing.getPushTokens().addAll(payload.getPushTokens());
        }

        // Vérifications & KYC (communs)
        existing.setEmailVerifie(payload.isEmailVerifie());
        existing.setTelephoneVerifie(payload.isTelephoneVerifie());
        existing.setIdentiteVerifiee(payload.isIdentiteVerifiee());
        existing.setRibVerifie(payload.isRibVerifie());
        if (payload.getKycStatut() != null) existing.setKycStatut(payload.getKycStatut());

        // *** Profil commun ***
        existing.setLocalisation(payload.getLocalisation());
        existing.setGouvernorat(payload.getGouvernorat());

        // *** ⬇️ Déplacé ici : MAJ URLS pour TOUS les types (FREELANCE & CLIENT) ***
        existing.setLinkedinUrl(normalizeUrl(payload.getLinkedinUrl())); // *** MODIF ***
        existing.setGithubUrl(normalizeUrl(payload.getGithubUrl()));     // *** MODIF ***

        /* -------- Spécifique FREELANCE -------- */
        if (existing.getTypeUtilisateur() == TypeUtilisateur.FREELANCE) {
            if (payload.getCompetences() != null) {
                Set<String> normalized = competenceService.normalizeSet(payload.getCompetences());
                log.info("[Utilisateur#update] Normalisation compétences: {} -> {}", payload.getCompetences(), normalized);
                existing.setCompetences(normalized);
            }
            existing.setTarifHoraire(payload.getTarifHoraire());
            existing.setTarifJournalier(payload.getTarifJournalier());
            existing.setDisponibilite(payload.getDisponibilite());
            existing.setBio(payload.getBio());
            existing.setNiveauExperience(payload.getNiveauExperience());
            if (payload.getPortfolioUrls() != null) existing.setPortfolioUrls(payload.getPortfolioUrls());
            if (payload.getListeBadges() != null) existing.setListeBadges(payload.getListeBadges());
            existing.setNoteMoyenne(payload.getNoteMoyenne());
            existing.setProjetsTermines(payload.getProjetsTermines());
            if (payload.getCategories() != null) existing.setCategories(payload.getCategories());

            // Nouveaux attributs profil freelance
            existing.setTitreProfil(payload.getTitreProfil());
            existing.setAnneesExperience(payload.getAnneesExperience());
            existing.setTimezone(
                StringUtils.hasText(payload.getTimezone()) ? payload.getTimezone() : existing.getTimezone()
            );
            existing.setMobilite(payload.getMobilite());
            existing.setDateDisponibilite(payload.getDateDisponibilite());
            existing.setChargeHebdoSouhaiteeJours(payload.getChargeHebdoSouhaiteeJours());

            if (payload.getLangues() != null) existing.setLangues(payload.getLangues());
            if (payload.getCompetencesNiveaux() != null) existing.setCompetencesNiveaux(payload.getCompetencesNiveaux());
            if (payload.getModelesEngagementPreferes() != null) existing.setModelesEngagementPreferes(payload.getModelesEngagementPreferes());

            if (payload.getPreferenceDuree() != null) {
                existing.setPreferenceDuree(payload.getPreferenceDuree());
            }
            if (payload.getNombreAvis() != null) {
                existing.setNombreAvis(Math.max(0, payload.getNombreAvis()));
            }

            existing.setFlexibiliteTarifairePourcent(payload.getFlexibiliteTarifairePourcent());

            existing.setTauxReussite(payload.getTauxReussite());
            existing.setTauxRespectDelais(payload.getTauxRespectDelais());
            existing.setTauxReembauche(payload.getTauxReembauche());
            existing.setDelaiReponseHeures(payload.getDelaiReponseHeures());
            existing.setDelaiReponseMedianMinutes(payload.getDelaiReponseMedianMinutes());

            if (payload.getCertifications() != null) existing.setCertifications(payload.getCertifications());

            existing.setAutoriseContactAvantMatch(payload.isAutoriseContactAvantMatch());

            // Gamification / limites
            if (payload.getQuotaSwipesQuotidien() != null) existing.setQuotaSwipesQuotidien(payload.getQuotaSwipesQuotidien());
            if (payload.getQuotaSwipesDernierReset() != null) existing.setQuotaSwipesDernierReset(payload.getQuotaSwipesDernierReset());
            if (payload.getSuperLikesRestantsDuJour() != null) existing.setSuperLikesRestantsDuJour(payload.getSuperLikesRestantsDuJour());
            if (payload.getDernierSuperLikeAt() != null) existing.setDernierSuperLikeAt(payload.getDernierSuperLikeAt());

            if (payload.getSignalementsRecus() != null) existing.setSignalementsRecus(payload.getSignalementsRecus());
            existing.setSuspicionFraudeScore(payload.getSuspicionFraudeScore());
        }

        /* -------- Spécifique CLIENT -------- */
        if (existing.getTypeUtilisateur() == TypeUtilisateur.CLIENT) {
            existing.setNomEntreprise(payload.getNomEntreprise());
            existing.setSiteEntreprise(payload.getSiteEntreprise());
            existing.setDescriptionEntreprise(payload.getDescriptionEntreprise());
            existing.setMissionsPubliees(payload.getMissionsPubliees());
            if (payload.getHistoriqueMissions() != null)
                existing.setHistoriqueMissions(payload.getHistoriqueMissions());
            existing.setNoteDonneeMoy(payload.getNoteDonneeMoy());

            // Attributs client Tunisie / paiement
            existing.setDelaiPaiementMoyenJours(payload.getDelaiPaiementMoyenJours());
            existing.setFiabilitePaiement(payload.getFiabilitePaiement());
            existing.setPrestataireEscrowFavori(payload.getPrestataireEscrowFavori());
            existing.setExigeDepotAvantChat(payload.isExigeDepotAvantChat());
        }

        sanitizeNumericRanges(existing);
        ensureDailySuperlikesReset(existing);

        existing.setDerniereMiseAJour(LocalDateTime.now());
        return utilisateurRepository.save(existing);
    }

    /* =========================================================
       4. Mise à jour partielle (PATCH profil)
       ========================================================= */
    @Transactional
    public Utilisateur patchProfil(Long id,
                                   String bio,
                                   String localisation,
                                   Set<String> competences,
                                   Double tarifHoraire,
                                   Double tarifJournalier,
                                   Set<Mission.Categorie> categories,
                                   String photoProfilUrl) {

        Utilisateur u = getUtilisateurById(id);

        if (bio != null) u.setBio(bio);
        if (localisation != null) u.setLocalisation(localisation);
        if (photoProfilUrl != null) u.setPhotoProfilUrl(photoProfilUrl);
        if (tarifHoraire != null && tarifHoraire > 0) u.setTarifHoraire(tarifHoraire);
        if (tarifJournalier != null && tarifJournalier > 0) u.setTarifJournalier(tarifJournalier);
        if (competences != null && !competences.isEmpty()) {
            Set<String> normalized = competenceService.normalizeSet(competences);
            log.info("[Utilisateur#patchProfil] Normalisation compétences: {} -> {}", competences, normalized);
            u.setCompetences(normalized);
        }
        if (categories != null && !categories.isEmpty()) u.setCategories(categories);

        u.setDerniereMiseAJour(LocalDateTime.now());
        return utilisateurRepository.save(u);
    }

    /* =========================================================
       5. Activation / Désactivation
       ========================================================= */
    @Transactional
    public void setActive(Long id, boolean actif) {
        Utilisateur u = getUtilisateurById(id);
        u.setEstActif(actif);
        u.setDerniereMiseAJour(LocalDateTime.now());
        utilisateurRepository.save(u);
    }

    /* =========================================================
       6. Push tokens
       ========================================================= */
    @Transactional
    public void addPushToken(Long userId, String token) {
        if (!StringUtils.hasText(token)) return;
        Utilisateur u = getUtilisateurById(userId);
        u.getPushTokens().add(token);
        utilisateurRepository.save(u);
    }

    @Transactional
    public void removePushToken(Long userId, String token) {
        Utilisateur u = getUtilisateurById(userId);
        if (u.getPushTokens().remove(token)) {
            utilisateurRepository.save(u);
        }
    }

    /* =========================================================
       7. Suppression
       ========================================================= */
    @Transactional
    public void deleteUtilisateur(Long id) {
        if (!utilisateurRepository.existsById(id))
            throw new EntityNotFoundException("Impossible de supprimer, utilisateur introuvable avec l'id " + id);
        utilisateurRepository.deleteById(id);
    }

    /* =========================================================
       8. Compteurs
       ========================================================= */
    @Transactional
    public void incrementSwipe(Long userId) {
        Utilisateur u = getUtilisateurById(userId);
        ensureDailySuperlikesReset(u);
        u.incrementNombreSwipes();
        utilisateurRepository.save(u);
    }

    @Transactional
    public void incrementLikeRecu(Long freelanceId) {
        Utilisateur u = getUtilisateurById(freelanceId);
        u.incrementLikesRecus();
        utilisateurRepository.save(u);
    }

    @Transactional
    public void incrementMatch(Long userId) {
        Utilisateur u = getUtilisateurById(userId);
        u.incrementMatchesObtenus();
        utilisateurRepository.save(u);
    }

    /* =========================================================
       8bis. Gamification
       ========================================================= */
    @Transactional
    public void consumeSuperLike(Long userId) {
        Utilisateur u = getUtilisateurById(userId);
        ensureDailySuperlikesReset(u);
        Integer restants = u.getSuperLikesRestantsDuJour();
        if (restants != null && restants > 0) {
            u.setSuperLikesRestantsDuJour(restants - 1);
            u.setDernierSuperLikeAt(LocalDateTime.now());
            utilisateurRepository.save(u);
        } else {
            throw new IllegalStateException("Plus de super-likes disponibles aujourd’hui");
        }
    }

    @Transactional
    public void setDailySuperlikes(Long userId, int count) {
        Utilisateur u = getUtilisateurById(userId);
        u.setSuperLikesRestantsDuJour(Math.max(0, count));
        u.setDernierSuperLikeAt(LocalDateTime.now());
        utilisateurRepository.save(u);
    }

    /* =========================================================
       9. Validation interne
       ========================================================= */
    private void validateUtilisateur(Utilisateur utilisateur, boolean isNew) {

        if (!StringUtils.hasText(utilisateur.getNom()))
            throw new IllegalArgumentException("Le nom est obligatoire.");
        if (!StringUtils.hasText(utilisateur.getPrenom()))
            throw new IllegalArgumentException("Le prénom est obligatoire.");

        if (!StringUtils.hasText(utilisateur.getEmail()) ||
                !EMAIL_PATTERN.matcher(utilisateur.getEmail()).matches())
            throw new IllegalArgumentException("Email invalide.");

        if (isNew && utilisateurRepository.existsByEmail(utilisateur.getEmail()))
            throw new IllegalArgumentException("Email déjà utilisé.");

        // Mot de passe : on suppose hash
        if (isNew) {
            if (!StringUtils.hasText(utilisateur.getMotDePasse()) ||
                    utilisateur.getMotDePasse().length() < 40)
                throw new IllegalArgumentException("Mot de passe (hash) invalide.");
        }

        if (StringUtils.hasText(utilisateur.getNumeroTelephone()) &&
            !PHONE_PATTERN.matcher(utilisateur.getNumeroTelephone()).matches())
            throw new IllegalArgumentException("Numéro de téléphone invalide.");

        if (utilisateur.getTypeUtilisateur() == null)
            throw new IllegalArgumentException("Le type d'utilisateur est requis.");

        // Validation URLs pro si présentes
        if (StringUtils.hasText(utilisateur.getLinkedinUrl()) &&
            !URL_PATTERN.matcher(utilisateur.getLinkedinUrl()).matches()) {
            throw new IllegalArgumentException("URL LinkedIn invalide (attendu http(s)://...)");
        }
        if (StringUtils.hasText(utilisateur.getGithubUrl()) &&
            !URL_PATTERN.matcher(utilisateur.getGithubUrl()).matches()) {
            throw new IllegalArgumentException("URL GitHub invalide (attendu http(s)://...)");
        }
        if (utilisateur.getNombreAvis() != null && utilisateur.getNombreAvis() < 0) {
            throw new IllegalArgumentException("Le nombre d’avis doit être ≥ 0.");
        }

        /* -------- FREELANCE -------- */
        if (utilisateur.getTypeUtilisateur() == TypeUtilisateur.FREELANCE) {
            if (utilisateur.getCompetences() == null || utilisateur.getCompetences().isEmpty())
                throw new IllegalArgumentException("Au moins une compétence est requise pour un freelance.");

            if (utilisateur.getTarifHoraire() == null || utilisateur.getTarifHoraire() <= 0)
                throw new IllegalArgumentException("Le tarif horaire doit être positif.");

            if (utilisateur.getTarifJournalier() == null || utilisateur.getTarifJournalier() <= 0)
                throw new IllegalArgumentException("Le tarif journalier doit être positif.");

            if (utilisateur.getDisponibilite() == null)
                throw new IllegalArgumentException("Disponibilité requise.");

            if (utilisateur.getNiveauExperience() == null)
                throw new IllegalArgumentException("Niveau d'expérience requis.");

            if (!StringUtils.hasText(utilisateur.getLocalisation()))
                throw new IllegalArgumentException("Localisation requise.");

            if (utilisateur.getCategories() == null || utilisateur.getCategories().isEmpty())
                throw new IllegalArgumentException("Au moins une catégorie requise.");
        }

        /* -------- CLIENT -------- */
        if (utilisateur.getTypeUtilisateur() == TypeUtilisateur.CLIENT) {
            if (!StringUtils.hasText(utilisateur.getNomEntreprise()))
                throw new IllegalArgumentException("Le nom de l'entreprise est requis.");
        }
    }

    /* =========================================================
       10. Normalisation collections
       ========================================================= */
    private void normalizeCollections(Utilisateur u) {
        if (u.getCompetences() == null) u.setCompetences(new HashSet<>());
        if (u.getListeBadges() == null) u.setListeBadges(new HashSet<>());
        if (u.getPortfolioUrls() == null) u.setPortfolioUrls(new ArrayList<>());
        if (u.getCategories() == null) u.setCategories(new HashSet<>());
        if (u.getPushTokens() == null) u.setPushTokens(new HashSet<>());
        if (u.getHistoriqueMissions() == null) u.setHistoriqueMissions(new ArrayList<>());

        // Nouvelles collections/maps
        if (u.getLangues() == null) u.setLangues(new HashMap<>());
        if (u.getCompetencesNiveaux() == null) u.setCompetencesNiveaux(new HashMap<>());
        if (u.getModelesEngagementPreferes() == null) u.setModelesEngagementPreferes(new HashSet<>());
        if (u.getCertifications() == null) u.setCertifications(new ArrayList<>());
    }

    /* =========================================================
       11. Defaults Tunisie & garde-fous
       ========================================================= */
    private void applyTunisiaDefaults(Utilisateur u) {
        if (!StringUtils.hasText(u.getTimezone())) u.setTimezone(DEFAULT_TN_TIMEZONE);
        if (u.getLanguePref() == null) u.setLanguePref(Langue.FR);

        // Gamification : init
        if (u.getSuperLikesRestantsDuJour() == null) u.setSuperLikesRestantsDuJour(DEFAULT_DAILY_SUPERLIKES);
        if (u.getDernierSuperLikeAt() == null) u.setDernierSuperLikeAt(LocalDateTime.now());

        // Quota
        if (u.getQuotaSwipesQuotidien() == null) u.setQuotaSwipesQuotidien(200);
        if (u.getQuotaSwipesDernierReset() == null) u.setQuotaSwipesDernierReset(LocalDateTime.now());

        // Préférence durée par défaut
        if (u.getPreferenceDuree() == null) u.setPreferenceDuree(PreferenceDuree.INDIFFERENT);
        if (u.getNombreAvis() == null) u.setNombreAvis(0);
    }

    private void ensureDailySuperlikesReset(Utilisateur u) {
        if (u.getDernierSuperLikeAt() == null) {
            u.setSuperLikesRestantsDuJour(DEFAULT_DAILY_SUPERLIKES);
            u.setDernierSuperLikeAt(LocalDateTime.now());
            return;
        }
        LocalDate last = u.getDernierSuperLikeAt().toLocalDate();
        LocalDate now  = LocalDate.now();
        if (!now.isEqual(last)) {
            u.setSuperLikesRestantsDuJour(DEFAULT_DAILY_SUPERLIKES);
            u.setDernierSuperLikeAt(LocalDateTime.now());
        }
    }

    private void sanitizeNumericRanges(Utilisateur u) {
        if (u.getFlexibiliteTarifairePourcent() != null)
            u.setFlexibiliteTarifairePourcent(Math.max(0d, Math.min(100d, u.getFlexibiliteTarifairePourcent())));
        if (u.getTauxReussite() != null)
            u.setTauxReussite(Math.max(0d, Math.min(100d, u.getTauxReussite())));
        if (u.getTauxRespectDelais() != null)
            u.setTauxRespectDelais(Math.max(0d, Math.min(100d, u.getTauxRespectDelais())));
        if (u.getTauxReembauche() != null)
            u.setTauxReembauche(Math.max(0d, Math.min(100d, u.getTauxReembauche())));
        if (u.getFiabilitePaiement() != null)
            u.setFiabilitePaiement(Math.max(0d, Math.min(100d, u.getFiabilitePaiement())));

        if (u.getDelaiReponseHeures() != null && u.getDelaiReponseHeures() < 0)
            u.setDelaiReponseHeures(0);
        if (u.getDelaiReponseMedianMinutes() != null && u.getDelaiReponseMedianMinutes() < 0)
            u.setDelaiReponseMedianMinutes(0);
        if (u.getDelaiPaiementMoyenJours() != null && u.getDelaiPaiementMoyenJours() < 0)
            u.setDelaiPaiementMoyenJours(0);
        if (u.getAnneesExperience() != null && u.getAnneesExperience() < 0)
            u.setAnneesExperience(0);

        if (u.getNombreAvis() != null && u.getNombreAvis() < 0)
            u.setNombreAvis(0);
    }

    /* =========================================================
       12. Ops vérification ciblée
       ========================================================= */
    @Transactional
    public void verifyEmail(Long userId) {
        Utilisateur u = getUtilisateurById(userId);
        u.setEmailVerifie(true);
        utilisateurRepository.save(u);
    }

    @Transactional
    public void verifyPhone(Long userId) {
        Utilisateur u = getUtilisateurById(userId);
        u.setTelephoneVerifie(true);
        utilisateurRepository.save(u);
    }

    @Transactional
    public void setKycStatus(Long userId, StatutKyc statut) {
        Utilisateur u = getUtilisateurById(userId);
        u.setKycStatut(statut);
        utilisateurRepository.save(u);
    }

    /* =========================================================
       13. Helpers URLs
       ========================================================= */
    private String normalizeUrl(String url) {
        if (!StringUtils.hasText(url)) return null;
        String trimmed = url.trim();
        return URL_PATTERN.matcher(trimmed).matches() ? trimmed : trimmed;
    }
}

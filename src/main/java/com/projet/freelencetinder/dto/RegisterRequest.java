package com.projet.freelencetinder.dto;

import java.util.List;
import java.util.Map;

import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.models.Mission.Gouvernorat;
import com.projet.freelencetinder.models.Utilisateur.*;
import jakarta.validation.constraints.*;

/**
 * DTO d’inscription enrichi : permet de créer un utilisateur
 * (freelance ou client) avec la plupart des attributs de profil.
 */
public class RegisterRequest {

    /* ---------- Champs communs ---------- */
    @NotBlank @Size(min = 3,  max = 50)  private String nom;
    @NotBlank @Size(min = 2,  max = 50)  private String prenom;
    @NotBlank @Email                     private String email;
    @NotBlank @Size(min = 6)             private String password;
    @NotNull                             private TypeUtilisateur typeUtilisateur;

    /* ---------- Nouveau : sous-type client ---------- */
    private TypeClient typeClient; // null sauf si typeUtilisateur == CLIENT

    /* ---------- Contact & profil ---------- */
    private String numeroTelephone;
    private String photoProfilUrl;
    private Langue languePref;

    /* ======== AJOUTS COMMUNS ======== */
    private Gouvernorat gouvernorat;            // gouvernorat rattaché à l'utilisateur
    @Size(max = 250) private String linkedinUrl; // réseaux pro (facultatifs)
    @Size(max = 250) private String githubUrl;

    /* ---------- Attributs FREELANCE ---------- */
    private List<String>        competences;
    private Double              tarifHoraire;
    private Double              tarifJournalier;
    private Disponibilite       disponibilite;
    @Size(max = 1000) private String  bio;
    private NiveauExperience    niveauExperience;
    private String              localisation;
    private List<Mission.Categorie> categories;
    private List<String>        portfolioUrls;
    private List<String>        pushTokens;

    /* ======== AJOUTS FREELANCE ======== */
    private String  titreProfil;
    private Integer anneesExperience;
    private String  timezone; // ex. Africa/Tunis
    private Mobilite mobilite;
    private String  dateDisponibilite;        // ISO string
    private Integer chargeHebdoSouhaiteeJours;

    private List<EngagementModel> modelesEngagementPreferes;
    private PreferenceDuree       preferenceDuree; // COURT_TERME / LONG_TERME / INDIFFERENT
    private Double                flexibiliteTarifairePourcent;

    private Map<Langue, NiveauLangue>     langues;              // FR/AR/EN + niveau
    private Map<String, NiveauMaitrise>   competencesNiveaux;   // skill -> niveau
    private List<String>                  certifications;

    // Compteurs/avis pour accompagner la note moyenne (optionnel, par défaut 0 côté serveur)
    private Integer nombreAvis;

    /* ---------- Attributs CLIENT ---------- */
    private String  nomEntreprise;
    private String  siteEntreprise;
    @Size(max = 1000) private String descriptionEntreprise;

    /* ---------- ctor vide ---------- */
    public RegisterRequest() {}

    /* ---------- Getters / Setters ---------- */

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public TypeUtilisateur getTypeUtilisateur() { return typeUtilisateur; }
    public void setTypeUtilisateur(TypeUtilisateur typeUtilisateur) { this.typeUtilisateur = typeUtilisateur; }

    public TypeClient getTypeClient() { return typeClient; }
    public void setTypeClient(TypeClient typeClient) { this.typeClient = typeClient; }

    public String getNumeroTelephone() { return numeroTelephone; }
    public void setNumeroTelephone(String numeroTelephone) { this.numeroTelephone = numeroTelephone; }

    public String getPhotoProfilUrl() { return photoProfilUrl; }
    public void setPhotoProfilUrl(String photoProfilUrl) { this.photoProfilUrl = photoProfilUrl; }

    public Langue getLanguePref() { return languePref; }
    public void setLanguePref(Langue languePref) { this.languePref = languePref; }

    public Gouvernorat getGouvernorat() { return gouvernorat; }
    public void setGouvernorat(Gouvernorat gouvernorat) { this.gouvernorat = gouvernorat; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public List<String> getCompetences() { return competences; }
    public void setCompetences(List<String> competences) { this.competences = competences; }

    public Double getTarifHoraire() { return tarifHoraire; }
    public void setTarifHoraire(Double tarifHoraire) { this.tarifHoraire = tarifHoraire; }

    public Double getTarifJournalier() { return tarifJournalier; }
    public void setTarifJournalier(Double tarifJournalier) { this.tarifJournalier = tarifJournalier; }

    public Disponibilite getDisponibilite() { return disponibilite; }
    public void setDisponibilite(Disponibilite disponibilite) { this.disponibilite = disponibilite; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public NiveauExperience getNiveauExperience() { return niveauExperience; }
    public void setNiveauExperience(NiveauExperience niveauExperience) { this.niveauExperience = niveauExperience; }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public List<Mission.Categorie> getCategories() { return categories; }
    public void setCategories(List<Mission.Categorie> categories) { this.categories = categories; }

    public List<String> getPortfolioUrls() { return portfolioUrls; }
    public void setPortfolioUrls(List<String> portfolioUrls) { this.portfolioUrls = portfolioUrls; }

    public List<String> getPushTokens() { return pushTokens; }
    public void setPushTokens(List<String> pushTokens) { this.pushTokens = pushTokens; }

    public String getTitreProfil() { return titreProfil; }
    public void setTitreProfil(String titreProfil) { this.titreProfil = titreProfil; }

    public Integer getAnneesExperience() { return anneesExperience; }
    public void setAnneesExperience(Integer anneesExperience) { this.anneesExperience = anneesExperience; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public Mobilite getMobilite() { return mobilite; }
    public void setMobilite(Mobilite mobilite) { this.mobilite = mobilite; }

    public String getDateDisponibilite() { return dateDisponibilite; }
    public void setDateDisponibilite(String dateDisponibilite) { this.dateDisponibilite = dateDisponibilite; }

    public Integer getChargeHebdoSouhaiteeJours() { return chargeHebdoSouhaiteeJours; }
    public void setChargeHebdoSouhaiteeJours(Integer chargeHebdoSouhaiteeJours) { this.chargeHebdoSouhaiteeJours = chargeHebdoSouhaiteeJours; }

    public List<EngagementModel> getModelesEngagementPreferes() { return modelesEngagementPreferes; }
    public void setModelesEngagementPreferes(List<EngagementModel> modelesEngagementPreferes) { this.modelesEngagementPreferes = modelesEngagementPreferes; }

    public PreferenceDuree getPreferenceDuree() { return preferenceDuree; }
    public void setPreferenceDuree(PreferenceDuree preferenceDuree) { this.preferenceDuree = preferenceDuree; }

    public Double getFlexibiliteTarifairePourcent() { return flexibiliteTarifairePourcent; }
    public void setFlexibiliteTarifairePourcent(Double flexibiliteTarifairePourcent) { this.flexibiliteTarifairePourcent = flexibiliteTarifairePourcent; }

    public Map<Langue, NiveauLangue> getLangues() { return langues; }
    public void setLangues(Map<Langue, NiveauLangue> langues) { this.langues = langues; }

    public Map<String, NiveauMaitrise> getCompetencesNiveaux() { return competencesNiveaux; }
    public void setCompetencesNiveaux(Map<String, NiveauMaitrise> competencesNiveaux) { this.competencesNiveaux = competencesNiveaux; }

    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }

    public Integer getNombreAvis() { return nombreAvis; }
    public void setNombreAvis(Integer nombreAvis) { this.nombreAvis = nombreAvis; }

    public String getNomEntreprise() { return nomEntreprise; }
    public void setNomEntreprise(String nomEntreprise) { this.nomEntreprise = nomEntreprise; }

    public String getSiteEntreprise() { return siteEntreprise; }
    public void setSiteEntreprise(String siteEntreprise) { this.siteEntreprise = siteEntreprise; }

    public String getDescriptionEntreprise() { return descriptionEntreprise; }
    public void setDescriptionEntreprise(String descriptionEntreprise) { this.descriptionEntreprise = descriptionEntreprise; }
}

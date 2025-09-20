package com.projet.freelencetinder.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.projet.freelencetinder.models.Mission; // pour Gouvernorat
import com.projet.freelencetinder.models.Utilisateur.Disponibilite;
import com.projet.freelencetinder.models.Utilisateur.EngagementModel;
import com.projet.freelencetinder.models.Utilisateur.Langue;
import com.projet.freelencetinder.models.Utilisateur.Mobilite;
import com.projet.freelencetinder.models.Utilisateur.NiveauExperience;
import com.projet.freelencetinder.models.Utilisateur.NiveauLangue;
import com.projet.freelencetinder.models.Utilisateur.NiveauMaitrise;
import com.projet.freelencetinder.models.Utilisateur.PreferenceDuree;
import com.projet.freelencetinder.models.Utilisateur.StatutKyc;

/**
 * Résumé des informations d’un freelance à afficher dans la carte côté client.
 */
public class FreelanceSummaryDTO {

    private Long id;
    private String nom;
    private String prenom;
    private String photoUrl;
    private String localisation;
    private Mission.Gouvernorat gouvernorat;   // *** AJOUT ***
    private String niveauExperience;
    private String disponibilite;
    private Double tarifHoraire;
    private Double noteMoyenne;
    private Set<String> competences;
    private String badgePrincipal; // ex. Top Talent, Expert…

    /* ======== AJOUTS ======== */
    private String titreProfil;
    private Integer anneesExperience;
    private Mobilite mobilite;
    private String timezone;

    private Double tarifJournalier;
    private List<EngagementModel> modelesEngagementPreferes;

    private String dateDisponibilite;
    private Integer chargeHebdoSouhaiteeJours;

    private Map<Langue, NiveauLangue> langues;
    private Map<String, NiveauMaitrise> competencesNiveaux;

    private Double tauxReussite;
    private Double tauxRespectDelais;
    private Double tauxReembauche;
    private Integer delaiReponseHeures;
    private Integer delaiReponseMedianMinutes;

    private List<String> certifications;
    private List<String> portfolioUrls;   // *** AJOUT ***

    // Vérifications & KYC
    private boolean emailVerifie;
    private boolean telephoneVerifie;
    private boolean identiteVerifiee;
    private boolean ribVerifie;
    private StatutKyc kycStatut;

    // Matching par rapport à la mission en cours (optionnel côté back)
    private Double matchScore;
    private List<String> matchReasons;

    // ======== AJOUTS (nouveaux champs demandés) ========
    private PreferenceDuree preferenceDuree; // COURT_TERME / LONG_TERME / INDIFFERENT
    private Integer nombreAvis;             // accompagne noteMoyenne
    private String linkedinUrl;             // URL pro (icône)
    private String githubUrl;               // URL pro (icône)

    /* ===== Getters / Setters ===== */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public Mission.Gouvernorat getGouvernorat() { return gouvernorat; }          // *** AJOUT ***
    public void setGouvernorat(Mission.Gouvernorat gouvernorat) { this.gouvernorat = gouvernorat; } // *** AJOUT ***

    public String getNiveauExperience() { return niveauExperience; }
    public void setNiveauExperience(String niveauExperience) { this.niveauExperience = niveauExperience; }

    public String getDisponibilite() { return disponibilite; }
    public void setDisponibilite(String disponibilite) { this.disponibilite = disponibilite; }

    public Double getTarifHoraire() { return tarifHoraire; }
    public void setTarifHoraire(Double tarifHoraire) { this.tarifHoraire = tarifHoraire; }

    public Double getNoteMoyenne() { return noteMoyenne; }
    public void setNoteMoyenne(Double noteMoyenne) { this.noteMoyenne = noteMoyenne; }

    public Set<String> getCompetences() { return competences; }
    public void setCompetences(Set<String> competences) { this.competences = competences; }

    public String getBadgePrincipal() { return badgePrincipal; }
    public void setBadgePrincipal(String badgePrincipal) { this.badgePrincipal = badgePrincipal; }

    public String getTitreProfil() { return titreProfil; }
    public void setTitreProfil(String titreProfil) { this.titreProfil = titreProfil; }

    public Integer getAnneesExperience() { return anneesExperience; }
    public void setAnneesExperience(Integer anneesExperience) { this.anneesExperience = anneesExperience; }

    public Mobilite getMobilite() { return mobilite; }
    public void setMobilite(Mobilite mobilite) { this.mobilite = mobilite; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public Double getTarifJournalier() { return tarifJournalier; }
    public void setTarifJournalier(Double tarifJournalier) { this.tarifJournalier = tarifJournalier; }

    public List<EngagementModel> getModelesEngagementPreferes() { return modelesEngagementPreferes; }
    public void setModelesEngagementPreferes(List<EngagementModel> modelesEngagementPreferes) { this.modelesEngagementPreferes = modelesEngagementPreferes; }

    public String getDateDisponibilite() { return dateDisponibilite; }
    public void setDateDisponibilite(String dateDisponibilite) { this.dateDisponibilite = dateDisponibilite; }

    public Integer getChargeHebdoSouhaiteeJours() { return chargeHebdoSouhaiteeJours; }
    public void setChargeHebdoSouhaiteeJours(Integer chargeHebdoSouhaiteeJours) { this.chargeHebdoSouhaiteeJours = chargeHebdoSouhaiteeJours; }

    public Map<Langue, NiveauLangue> getLangues() { return langues; }
    public void setLangues(Map<Langue, NiveauLangue> langues) { this.langues = langues; }

    public Map<String, NiveauMaitrise> getCompetencesNiveaux() { return competencesNiveaux; }
    public void setCompetencesNiveaux(Map<String, NiveauMaitrise> competencesNiveaux) { this.competencesNiveaux = competencesNiveaux; }

    public Double getTauxReussite() { return tauxReussite; }
    public void setTauxReussite(Double tauxReussite) { this.tauxReussite = tauxReussite; }

    public Double getTauxRespectDelais() { return tauxRespectDelais; }
    public void setTauxRespectDelais(Double tauxRespectDelais) { this.tauxRespectDelais = tauxRespectDelais; }

    public Double getTauxReembauche() { return tauxReembauche; }
    public void setTauxReembauche(Double tauxReembauche) { this.tauxReembauche = tauxReembauche; }

    public Integer getDelaiReponseHeures() { return delaiReponseHeures; }
    public void setDelaiReponseHeures(Integer delaiReponseHeures) { this.delaiReponseHeures = delaiReponseHeures; }

    public Integer getDelaiReponseMedianMinutes() { return delaiReponseMedianMinutes; }
    public void setDelaiReponseMedianMinutes(Integer delaiReponseMedianMinutes) { this.delaiReponseMedianMinutes = delaiReponseMedianMinutes; }

    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }

    public List<String> getPortfolioUrls() { return portfolioUrls; }            // *** AJOUT ***
    public void setPortfolioUrls(List<String> portfolioUrls) { this.portfolioUrls = portfolioUrls; } // *** AJOUT ***

    public boolean isEmailVerifie() { return emailVerifie; }
    public void setEmailVerifie(boolean emailVerifie) { this.emailVerifie = emailVerifie; }

    public boolean isTelephoneVerifie() { return telephoneVerifie; }
    public void setTelephoneVerifie(boolean telephoneVerifie) { this.telephoneVerifie = telephoneVerifie; }

    public boolean isIdentiteVerifiee() { return identiteVerifiee; }
    public void setIdentiteVerifiee(boolean identiteVerifiee) { this.identiteVerifiee = identiteVerifiee; }

    public boolean isRibVerifie() { return ribVerifie; }
    public void setRibVerifie(boolean ribVerifie) { this.ribVerifie = ribVerifie; }

    public StatutKyc getKycStatut() { return kycStatut; }
    public void setKycStatut(StatutKyc kycStatut) { this.kycStatut = kycStatut; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public List<String> getMatchReasons() { return matchReasons; }
    public void setMatchReasons(List<String> matchReasons) { this.matchReasons = matchReasons; }

    public PreferenceDuree getPreferenceDuree() { return preferenceDuree; }
    public void setPreferenceDuree(PreferenceDuree preferenceDuree) { this.preferenceDuree = preferenceDuree; }

    public Integer getNombreAvis() { return nombreAvis; }
    public void setNombreAvis(Integer nombreAvis) { this.nombreAvis = nombreAvis; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }
}

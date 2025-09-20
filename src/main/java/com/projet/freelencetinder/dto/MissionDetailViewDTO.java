package com.projet.freelencetinder.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.models.Utilisateur;

/**
 * DTO pour la vue détail mission côté freelance.
 * Contient toutes les informations nécessaires avec masquage approprié selon le statut d'assignation.
 */
public class MissionDetailViewDTO {

    /* ===================== Identité & Version ===================== */
    private Long id;
    private Long version;

    /* ===================== Informations mission ===================== */
    private String titre;
    private String description;
    private Mission.Categorie categorie;
    private Mission.Statut statut;
    private boolean urgent;
    private boolean verrouillee;
    private Set<String> badges;
    private List<String> raisonsMatching;
    private Double scoreMatching;

    /* ===================== Budget & rémunération ===================== */
    private BigDecimal budget;
    private String devise;
    private Mission.TypeRemuneration typeRemuneration;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private BigDecimal tjmJournalier;

    /* ===================== Planning & charge ===================== */
    private LocalDate dateDebutSouhaitee;
    private LocalDate delaiLivraison;
    private Integer dureeEstimeeJours;
    private Integer chargeHebdoJours;
    private LocalDate dateLimiteCandidature;

    /* ===================== Localisation ===================== */
    private String localisation;
    private Mission.Gouvernorat gouvernorat;
    private Mission.ModaliteTravail modaliteTravail;

    /* ===================== Exigences ===================== */
    private Set<String> competencesRequises;
    private Map<String, String> competencesPriorisees; // MUST/NICE
    private Map<Utilisateur.Langue, Utilisateur.NiveauLangue> languesRequises;
    private Utilisateur.NiveauExperience niveauExperienceMin;
    private Mission.NiveauBrief qualiteBrief;

    /* ===================== Médias ===================== */
    private List<String> mediaUrls;
    private String videoBriefUrl;

    /* ===================== Métadonnées ===================== */
    private LocalDateTime datePublication;
    private LocalDateTime dateDerniereMiseAJour;
    private LocalDateTime derniereActiviteAt;
    private Integer swipesRecus;
    private Integer likesRecus;
    private Integer candidatsCount;

    /* ===================== Contexte viewer ===================== */
    public enum ViewerRole { VISITOR, FREELANCER, CLIENT }
    
    private Long viewerId;
    private ViewerRole viewerRole = ViewerRole.FREELANCER;
    private boolean viewerIsAssignedFreelancer;
    private boolean expired;

    ;
    private String clientNomComplet; // rempli seulement si assigné

    /* ===================== Freelance sélectionné ===================== */
    private String freelanceNomComplet; // si assigné

    /* ===================== Paiements (masqués si non assigné) ===================== */
    private PaymentMiniDTO paiements;

    /* ===================== Livrables ===================== */
    private List<LivrableLiteDTO> livrables;

    /* ===================== Permissions & actions ===================== */
    private FreelancePermissionsDTO permissions;
    
    public enum NextActionFreelancer { 
        APPLIQUER, 
        ATTENDRE_SELECTION, 
        ENVOYER_LIVRABLE, 
        ATTENDRE_VALIDATION, 
        ATTENDRE_PAIEMENT, 
        CONSULTER_PAIEMENTS, 
        MISSION_TERMINEE 
    }
    
    private NextActionFreelancer nextAction;

    /* ===================== Getters & Setters ===================== */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Mission.Categorie getCategorie() { return categorie; }
    public void setCategorie(Mission.Categorie categorie) { this.categorie = categorie; }

    public Mission.Statut getStatut() { return statut; }
    public void setStatut(Mission.Statut statut) { this.statut = statut; }

    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }

    public boolean isVerrouillee() { return verrouillee; }
    public void setVerrouillee(boolean verrouillee) { this.verrouillee = verrouillee; }

    public Set<String> getBadges() { return badges; }
    public void setBadges(Set<String> badges) { this.badges = badges; }

    public List<String> getRaisonsMatching() { return raisonsMatching; }
    public void setRaisonsMatching(List<String> raisonsMatching) { this.raisonsMatching = raisonsMatching; }

    public Double getScoreMatching() { return scoreMatching; }
    public void setScoreMatching(Double scoreMatching) { this.scoreMatching = scoreMatching; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public Mission.TypeRemuneration getTypeRemuneration() { return typeRemuneration; }
    public void setTypeRemuneration(Mission.TypeRemuneration typeRemuneration) { this.typeRemuneration = typeRemuneration; }

    public BigDecimal getBudgetMin() { return budgetMin; }
    public void setBudgetMin(BigDecimal budgetMin) { this.budgetMin = budgetMin; }

    public BigDecimal getBudgetMax() { return budgetMax; }
    public void setBudgetMax(BigDecimal budgetMax) { this.budgetMax = budgetMax; }

    public BigDecimal getTjmJournalier() { return tjmJournalier; }
    public void setTjmJournalier(BigDecimal tjmJournalier) { this.tjmJournalier = tjmJournalier; }

    public LocalDate getDateDebutSouhaitee() { return dateDebutSouhaitee; }
    public void setDateDebutSouhaitee(LocalDate dateDebutSouhaitee) { this.dateDebutSouhaitee = dateDebutSouhaitee; }

    public LocalDate getDelaiLivraison() { return delaiLivraison; }
    public void setDelaiLivraison(LocalDate delaiLivraison) { this.delaiLivraison = delaiLivraison; }

    public Integer getDureeEstimeeJours() { return dureeEstimeeJours; }
    public void setDureeEstimeeJours(Integer dureeEstimeeJours) { this.dureeEstimeeJours = dureeEstimeeJours; }

    public Integer getChargeHebdoJours() { return chargeHebdoJours; }
    public void setChargeHebdoJours(Integer chargeHebdoJours) { this.chargeHebdoJours = chargeHebdoJours; }

    public LocalDate getDateLimiteCandidature() { return dateLimiteCandidature; }
    public void setDateLimiteCandidature(LocalDate dateLimiteCandidature) { this.dateLimiteCandidature = dateLimiteCandidature; }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public Mission.Gouvernorat getGouvernorat() { return gouvernorat; }
    public void setGouvernorat(Mission.Gouvernorat gouvernorat) { this.gouvernorat = gouvernorat; }

    public Mission.ModaliteTravail getModaliteTravail() { return modaliteTravail; }
    public void setModaliteTravail(Mission.ModaliteTravail modaliteTravail) { this.modaliteTravail = modaliteTravail; }

    public Set<String> getCompetencesRequises() { return competencesRequises; }
    public void setCompetencesRequises(Set<String> competencesRequises) { this.competencesRequises = competencesRequises; }

    public Map<String, String> getCompetencesPriorisees() { return competencesPriorisees; }
    public void setCompetencesPriorisees(Map<String, String> competencesPriorisees) { this.competencesPriorisees = competencesPriorisees; }

    public Map<Utilisateur.Langue, Utilisateur.NiveauLangue> getLanguesRequises() { return languesRequises; }
    public void setLanguesRequises(Map<Utilisateur.Langue, Utilisateur.NiveauLangue> languesRequises) { this.languesRequises = languesRequises; }

    public Utilisateur.NiveauExperience getNiveauExperienceMin() { return niveauExperienceMin; }
    public void setNiveauExperienceMin(Utilisateur.NiveauExperience niveauExperienceMin) { this.niveauExperienceMin = niveauExperienceMin; }

    public Mission.NiveauBrief getQualiteBrief() { return qualiteBrief; }
    public void setQualiteBrief(Mission.NiveauBrief qualiteBrief) { this.qualiteBrief = qualiteBrief; }

    public List<String> getMediaUrls() { return mediaUrls; }
    public void setMediaUrls(List<String> mediaUrls) { this.mediaUrls = mediaUrls; }

    public String getVideoBriefUrl() { return videoBriefUrl; }
    public void setVideoBriefUrl(String videoBriefUrl) { this.videoBriefUrl = videoBriefUrl; }

    public LocalDateTime getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDateTime datePublication) { this.datePublication = datePublication; }

    public LocalDateTime getDateDerniereMiseAJour() { return dateDerniereMiseAJour; }
    public void setDateDerniereMiseAJour(LocalDateTime dateDerniereMiseAJour) { this.dateDerniereMiseAJour = dateDerniereMiseAJour; }

    public LocalDateTime getDerniereActiviteAt() { return derniereActiviteAt; }
    public void setDerniereActiviteAt(LocalDateTime derniereActiviteAt) { this.derniereActiviteAt = derniereActiviteAt; }

    public Integer getSwipesRecus() { return swipesRecus; }
    public void setSwipesRecus(Integer swipesRecus) { this.swipesRecus = swipesRecus; }

    public Integer getLikesRecus() { return likesRecus; }
    public void setLikesRecus(Integer likesRecus) { this.likesRecus = likesRecus; }

    public Integer getCandidatsCount() { return candidatsCount; }
    public void setCandidatsCount(Integer candidatsCount) { this.candidatsCount = candidatsCount; }

    public Long getViewerId() { return viewerId; }
    public void setViewerId(Long viewerId) { this.viewerId = viewerId; }

    public ViewerRole getViewerRole() { return viewerRole; }
    public void setViewerRole(ViewerRole viewerRole) { this.viewerRole = viewerRole; }

    public boolean isViewerIsAssignedFreelancer() { return viewerIsAssignedFreelancer; }
    public void setViewerIsAssignedFreelancer(boolean viewerIsAssignedFreelancer) { this.viewerIsAssignedFreelancer = viewerIsAssignedFreelancer; }

    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }



    public String getClientNomComplet() { return clientNomComplet; }
    public void setClientNomComplet(String clientNomComplet) { this.clientNomComplet = clientNomComplet; }

    public String getFreelanceNomComplet() { return freelanceNomComplet; }
    public void setFreelanceNomComplet(String freelanceNomComplet) { this.freelanceNomComplet = freelanceNomComplet; }

    public PaymentMiniDTO getPaiements() { return paiements; }
    public void setPaiements(PaymentMiniDTO paiements) { this.paiements = paiements; }

    public List<LivrableLiteDTO> getLivrables() { return livrables; }
    public void setLivrables(List<LivrableLiteDTO> livrables) { this.livrables = livrables; }

    public FreelancePermissionsDTO getPermissions() { return permissions; }
    public void setPermissions(FreelancePermissionsDTO permissions) { this.permissions = permissions; }

    public NextActionFreelancer getNextAction() { return nextAction; }
    public void setNextAction(NextActionFreelancer nextAction) { this.nextAction = nextAction; }

    /* ===================== Champs de clôture (AJOUT) ===================== */
    private Mission.ClosurePolicy closurePolicy;
    private boolean closedByClient;
    private boolean closedByFreelancer;
    private BigDecimal contractTotalAmount;

    public Mission.ClosurePolicy getClosurePolicy() { return closurePolicy; }
    public void setClosurePolicy(Mission.ClosurePolicy closurePolicy) { this.closurePolicy = closurePolicy; }

    public boolean isClosedByClient() { return closedByClient; }
    public void setClosedByClient(boolean closedByClient) { this.closedByClient = closedByClient; }

    public boolean isClosedByFreelancer() { return closedByFreelancer; }
    public void setClosedByFreelancer(boolean closedByFreelancer) { this.closedByFreelancer = closedByFreelancer; }

    public BigDecimal getContractTotalAmount() { return contractTotalAmount; }
    public void setContractTotalAmount(BigDecimal contractTotalAmount) { this.contractTotalAmount = contractTotalAmount; }
}
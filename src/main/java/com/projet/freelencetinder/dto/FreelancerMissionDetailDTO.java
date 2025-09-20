package com.projet.freelencetinder.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.projet.freelencetinder.models.Mission.Categorie;
import com.projet.freelencetinder.models.Mission.Gouvernorat;
import com.projet.freelencetinder.models.Mission.ModaliteTravail;
import com.projet.freelencetinder.models.Mission.NiveauBrief;
import com.projet.freelencetinder.models.Mission.Statut;
import com.projet.freelencetinder.models.Mission.TypeRemuneration;
import com.projet.freelencetinder.models.Utilisateur.Langue;
import com.projet.freelencetinder.models.Utilisateur.NiveauExperience;
import com.projet.freelencetinder.models.Utilisateur.NiveauLangue;

/**
 * Vue détaillée d'une mission côté freelance (lecture seule).
 */
public class FreelancerMissionDetailDTO {

    /* Identité & base */
    private Long id;
    private String titre;
    private String description;
    private Categorie categorie;
    private Statut statut;

    /* Exigences & matching */
    private List<String> competencesRequises;
    private Map<String, String> competencesPriorisees;
    private Map<Langue, NiveauLangue> languesRequises;
    private NiveauExperience niveauExperienceMin;
    private Double scoreMatching;
    private List<String> raisonsMatching;

    /* Budget & rémunération */
    private BigDecimal budget;
    private String devise;
    private TypeRemuneration typeRemuneration;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private BigDecimal tjmJournalier;

    /* Planning / charge */
    private LocalDate delaiLivraison;
    private Integer dureeEstimeeJours;
    private LocalDate dateLimiteCandidature;
    private LocalDate dateDebutSouhaitee;
    private Integer chargeHebdoJours;

    /* Localisation / modalité (Tunisie) */
    private String localisation;
    private Gouvernorat gouvernorat;
    private ModaliteTravail modaliteTravail;

    /* Qualité brief & activité */
    private boolean urgent;
    private NiveauBrief qualiteBrief;
    private LocalDateTime derniereActiviteAt;
    private boolean expired;

    /* Stats & badges */
    private Integer candidatsCount;
    private Integer swipesRecus;
    private Integer likesRecus;
    private Set<String> badges;

    /* Médias */
    private List<String> mediaUrls;
    private String videoBriefUrl;

    /* Parties prenantes */
    private ClientInfoDTO client;

    /* Contexte freelance */
    private boolean selectionne;     // le viewer est-il le freelance sélectionné ?
    private boolean canDeliver;      // peut-il livrer maintenant ?

    /* Paiements & livrables (visibles uniquement si sélectionné) */
    private PaymentMiniDTO paiements;
    private List<LivrableLiteDTO> livrables;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public List<String> getCompetencesRequises() { return competencesRequises; }
    public void setCompetencesRequises(List<String> competencesRequises) { this.competencesRequises = competencesRequises; }

    public Map<String, String> getCompetencesPriorisees() { return competencesPriorisees; }
    public void setCompetencesPriorisees(Map<String, String> competencesPriorisees) { this.competencesPriorisees = competencesPriorisees; }

    public Map<Langue, NiveauLangue> getLanguesRequises() { return languesRequises; }
    public void setLanguesRequises(Map<Langue, NiveauLangue> languesRequises) { this.languesRequises = languesRequises; }

    public NiveauExperience getNiveauExperienceMin() { return niveauExperienceMin; }
    public void setNiveauExperienceMin(NiveauExperience niveauExperienceMin) { this.niveauExperienceMin = niveauExperienceMin; }

    public Double getScoreMatching() { return scoreMatching; }
    public void setScoreMatching(Double scoreMatching) { this.scoreMatching = scoreMatching; }

    public List<String> getRaisonsMatching() { return raisonsMatching; }
    public void setRaisonsMatching(List<String> raisonsMatching) { this.raisonsMatching = raisonsMatching; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public TypeRemuneration getTypeRemuneration() { return typeRemuneration; }
    public void setTypeRemuneration(TypeRemuneration typeRemuneration) { this.typeRemuneration = typeRemuneration; }

    public BigDecimal getBudgetMin() { return budgetMin; }
    public void setBudgetMin(BigDecimal budgetMin) { this.budgetMin = budgetMin; }

    public BigDecimal getBudgetMax() { return budgetMax; }
    public void setBudgetMax(BigDecimal budgetMax) { this.budgetMax = budgetMax; }

    public BigDecimal getTjmJournalier() { return tjmJournalier; }
    public void setTjmJournalier(BigDecimal tjmJournalier) { this.tjmJournalier = tjmJournalier; }

    public LocalDate getDelaiLivraison() { return delaiLivraison; }
    public void setDelaiLivraison(LocalDate delaiLivraison) { this.delaiLivraison = delaiLivraison; }

    public Integer getDureeEstimeeJours() { return dureeEstimeeJours; }
    public void setDureeEstimeeJours(Integer dureeEstimeeJours) { this.dureeEstimeeJours = dureeEstimeeJours; }

    public LocalDate getDateLimiteCandidature() { return dateLimiteCandidature; }
    public void setDateLimiteCandidature(LocalDate dateLimiteCandidature) { this.dateLimiteCandidature = dateLimiteCandidature; }

    public LocalDate getDateDebutSouhaitee() { return dateDebutSouhaitee; }
    public void setDateDebutSouhaitee(LocalDate dateDebutSouhaitee) { this.dateDebutSouhaitee = dateDebutSouhaitee; }

    public Integer getChargeHebdoJours() { return chargeHebdoJours; }
    public void setChargeHebdoJours(Integer chargeHebdoJours) { this.chargeHebdoJours = chargeHebdoJours; }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public Gouvernorat getGouvernorat() { return gouvernorat; }
    public void setGouvernorat(Gouvernorat gouvernorat) { this.gouvernorat = gouvernorat; }

    public ModaliteTravail getModaliteTravail() { return modaliteTravail; }
    public void setModaliteTravail(ModaliteTravail modaliteTravail) { this.modaliteTravail = modaliteTravail; }

    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }

    public NiveauBrief getQualiteBrief() { return qualiteBrief; }
    public void setQualiteBrief(NiveauBrief qualiteBrief) { this.qualiteBrief = qualiteBrief; }

    public LocalDateTime getDerniereActiviteAt() { return derniereActiviteAt; }
    public void setDerniereActiviteAt(LocalDateTime derniereActiviteAt) { this.derniereActiviteAt = derniereActiviteAt; }

    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }

    public Integer getCandidatsCount() { return candidatsCount; }
    public void setCandidatsCount(Integer candidatsCount) { this.candidatsCount = candidatsCount; }

    public Integer getSwipesRecus() { return swipesRecus; }
    public void setSwipesRecus(Integer swipesRecus) { this.swipesRecus = swipesRecus; }

    public Integer getLikesRecus() { return likesRecus; }
    public void setLikesRecus(Integer likesRecus) { this.likesRecus = likesRecus; }

    public Set<String> getBadges() { return badges; }
    public void setBadges(Set<String> badges) { this.badges = badges; }

    public List<String> getMediaUrls() { return mediaUrls; }
    public void setMediaUrls(List<String> mediaUrls) { this.mediaUrls = mediaUrls; }

    public String getVideoBriefUrl() { return videoBriefUrl; }
    public void setVideoBriefUrl(String videoBriefUrl) { this.videoBriefUrl = videoBriefUrl; }

    public ClientInfoDTO getClient() { return client; }
    public void setClient(ClientInfoDTO client) { this.client = client; }

    public boolean isSelectionne() { return selectionne; }
    public void setSelectionne(boolean selectionne) { this.selectionne = selectionne; }

    public boolean isCanDeliver() { return canDeliver; }
    public void setCanDeliver(boolean canDeliver) { this.canDeliver = canDeliver; }

    public PaymentMiniDTO getPaiements() { return paiements; }
    public void setPaiements(PaymentMiniDTO paiements) { this.paiements = paiements; }

    public List<LivrableLiteDTO> getLivrables() { return livrables; }
    public void setLivrables(List<LivrableLiteDTO> livrables) { this.livrables = livrables; }
}



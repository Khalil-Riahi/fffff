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
import com.projet.freelencetinder.models.Mission.ClosurePolicy;

public class MissionDetailDTO {

    /* ---------- Identité & base ---------- */
    private Long id;
    private String titre;
    private String description;

    /* ---------- Compétences & matching ---------- */
    private List<String> competencesRequises;
    private int totalRequiredSkills;
    private int matchedSkills;
    private double matchRatio;          // 0.0 à 1.0
    private Double scoreMatching;       // Score calculé (optionnel)

    /* ---------- Budget & temps ---------- */
    private BigDecimal budget;
    private String devise;
    private LocalDate delaiLivraison;
    private Integer dureeEstimeeJours;
    private LocalDate dateLimiteCandidature;

    /* ---------- Localisation / modalité ---------- */
    private String localisation;
    private ModaliteTravail modaliteTravail;

    /* ---------- Statut & dates ---------- */
    private Categorie categorie;
    private Statut statut;
    private boolean expired;
    private boolean urgent;             // dateLimite <= 3 jours
    private LocalDateTime datePublication;
    private LocalDateTime dateAffectation;

    /* ---------- Côté client / freelance ---------- */
    private Long clientId;
    private String clientNomComplet;
    private Long freelanceSelectionneId;
    private String freelanceSelectionneNomComplet;

    /* ---------- Média ---------- */
    private List<String> mediaUrls;
    private String videoBriefUrl;

    /* ---------- Swipe / matching dynamique ---------- */
    private boolean alreadySwiped;      // l’utilisateur courant a déjà swipé ?
    private boolean likedByCurrentUser; // il a LIKE ?
    private boolean mutualMatch;        // match conclu ?
    private boolean missionLocked;      // plus swipable ?
    private Integer swipesRecus;
    private Integer likesRecus;

    /* ---------- Divers / UX ---------- */
    private boolean canSwipe;           // calculé serveur -> front simple
    private boolean canApply;           // si plus tard candidature
    private boolean selectionFaite;     // (statut EN_COURS)
    private String resumeCourt;         // [OPTION] snippet description

    /* ======== AJOUTS ======== */

    // Priorisation compétences & langues requises
    private Map<String, String> competencesPriorisees;                // competence -> MUST/NICE (stringified)
    private Map<Langue, NiveauLangue> languesRequises;
    private NiveauExperience niveauExperienceMin;

    // Rémunération
    private TypeRemuneration typeRemuneration;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private BigDecimal tjmJournalier;

    // Planning / charge
    private LocalDate dateDebutSouhaitee;
    private Integer chargeHebdoJours;

    // Tunisie : gouvernorat & qualité de brief
    private Gouvernorat gouvernorat;
    private NiveauBrief qualiteBrief;

    // Activité & affichage
    private LocalDateTime derniereActiviteAt;
    private Integer candidatsCount;
    private Set<String> badges;
    private List<String> raisonsMatching;

    public MissionDetailDTO() {}

    /* ===================== Getters / Setters ===================== */
    // (existants inchangés…)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getCompetencesRequises() { return competencesRequises; }
    public void setCompetencesRequises(List<String> competencesRequises) { this.competencesRequises = competencesRequises; }

    public int getTotalRequiredSkills() { return totalRequiredSkills; }
    public void setTotalRequiredSkills(int totalRequiredSkills) { this.totalRequiredSkills = totalRequiredSkills; }

    public int getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(int matchedSkills) { this.matchedSkills = matchedSkills; }

    public double getMatchRatio() { return matchRatio; }
    public void setMatchRatio(double matchRatio) { this.matchRatio = matchRatio; }

    public Double getScoreMatching() { return scoreMatching; }
    public void setScoreMatching(Double scoreMatching) { this.scoreMatching = scoreMatching; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public LocalDate getDelaiLivraison() { return delaiLivraison; }
    public void setDelaiLivraison(LocalDate delaiLivraison) { this.delaiLivraison = delaiLivraison; }

    public Integer getDureeEstimeeJours() { return dureeEstimeeJours; }
    public void setDureeEstimeeJours(Integer dureeEstimeeJours) { this.dureeEstimeeJours = dureeEstimeeJours; }

    public LocalDate getDateLimiteCandidature() { return dateLimiteCandidature; }
    public void setDateLimiteCandidature(LocalDate dateLimiteCandidature) { this.dateLimiteCandidature = dateLimiteCandidature; }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public ModaliteTravail getModaliteTravail() { return modaliteTravail; }
    public void setModaliteTravail(ModaliteTravail modaliteTravail) { this.modaliteTravail = modaliteTravail; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }

    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }

    public LocalDateTime getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDateTime datePublication) { this.datePublication = datePublication; }

    public LocalDateTime getDateAffectation() { return dateAffectation; }
    public void setDateAffectation(LocalDateTime dateAffectation) { this.dateAffectation = dateAffectation; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientNomComplet() { return clientNomComplet; }
    public void setClientNomComplet(String clientNomComplet) { this.clientNomComplet = clientNomComplet; }

    public Long getFreelanceSelectionneId() { return freelanceSelectionneId; }
    public void setFreelanceSelectionneId(Long freelanceSelectionneId) { this.freelanceSelectionneId = freelanceSelectionneId; }

    public String getFreelanceSelectionneNomComplet() { return freelanceSelectionneNomComplet; }
    public void setFreelanceSelectionneNomComplet(String freelanceSelectionneNomComplet) { this.freelanceSelectionneNomComplet = freelanceSelectionneNomComplet; }

    public List<String> getMediaUrls() { return mediaUrls; }
    public void setMediaUrls(List<String> mediaUrls) { this.mediaUrls = mediaUrls; }

    public String getVideoBriefUrl() { return videoBriefUrl; }
    public void setVideoBriefUrl(String videoBriefUrl) { this.videoBriefUrl = videoBriefUrl; }

    public boolean isAlreadySwiped() { return alreadySwiped; }
    public void setAlreadySwiped(boolean alreadySwiped) { this.alreadySwiped = alreadySwiped; }

    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
    public void setLikedByCurrentUser(boolean likedByCurrentUser) { this.likedByCurrentUser = likedByCurrentUser; }

    public boolean isMutualMatch() { return mutualMatch; }
    public void setMutualMatch(boolean mutualMatch) { this.mutualMatch = mutualMatch; }

    public boolean isMissionLocked() { return missionLocked; }
    public void setMissionLocked(boolean missionLocked) { this.missionLocked = missionLocked; }

    public Integer getSwipesRecus() { return swipesRecus; }
    public void setSwipesRecus(Integer swipesRecus) { this.swipesRecus = swipesRecus; }

    public Integer getLikesRecus() { return likesRecus; }
    public void setLikesRecus(Integer likesRecus) { this.likesRecus = likesRecus; }

    public boolean isCanSwipe() { return canSwipe; }
    public void setCanSwipe(boolean canSwipe) { this.canSwipe = canSwipe; }

    public boolean isCanApply() { return canApply; }
    public void setCanApply(boolean canApply) { this.canApply = canApply; }

    public boolean isSelectionFaite() { return selectionFaite; }
    public void setSelectionFaite(boolean selectionFaite) { this.selectionFaite = selectionFaite; }

    public String getResumeCourt() { return resumeCourt; }
    public void setResumeCourt(String resumeCourt) { this.resumeCourt = resumeCourt; }

    // AJOUTS
    public Map<String, String> getCompetencesPriorisees() { return competencesPriorisees; }
    public void setCompetencesPriorisees(Map<String, String> competencesPriorisees) { this.competencesPriorisees = competencesPriorisees; }

    public Map<Langue, NiveauLangue> getLanguesRequises() { return languesRequises; }
    public void setLanguesRequises(Map<Langue, NiveauLangue> languesRequises) { this.languesRequises = languesRequises; }

    public NiveauExperience getNiveauExperienceMin() { return niveauExperienceMin; }
    public void setNiveauExperienceMin(NiveauExperience niveauExperienceMin) { this.niveauExperienceMin = niveauExperienceMin; }

    public TypeRemuneration getTypeRemuneration() { return typeRemuneration; }
    public void setTypeRemuneration(TypeRemuneration typeRemuneration) { this.typeRemuneration = typeRemuneration; }

    public BigDecimal getBudgetMin() { return budgetMin; }
    public void setBudgetMin(BigDecimal budgetMin) { this.budgetMin = budgetMin; }

    public BigDecimal getBudgetMax() { return budgetMax; }
    public void setBudgetMax(BigDecimal budgetMax) { this.budgetMax = budgetMax; }

    public BigDecimal getTjmJournalier() { return tjmJournalier; }
    public void setTjmJournalier(BigDecimal tjmJournalier) { this.tjmJournalier = tjmJournalier; }

    public LocalDate getDateDebutSouhaitee() { return dateDebutSouhaitee; }
    public void setDateDebutSouhaitee(LocalDate dateDebutSouhaitee) { this.dateDebutSouhaitee = dateDebutSouhaitee; }

    public Integer getChargeHebdoJours() { return chargeHebdoJours; }
    public void setChargeHebdoJours(Integer chargeHebdoJours) { this.chargeHebdoJours = chargeHebdoJours; }

    public Gouvernorat getGouvernorat() { return gouvernorat; }
    public void setGouvernorat(Gouvernorat gouvernorat) { this.gouvernorat = gouvernorat; }

    public NiveauBrief getQualiteBrief() { return qualiteBrief; }
    public void setQualiteBrief(NiveauBrief qualiteBrief) { this.qualiteBrief = qualiteBrief; }

    public LocalDateTime getDerniereActiviteAt() { return derniereActiviteAt; }
    public void setDerniereActiviteAt(LocalDateTime derniereActiviteAt) { this.derniereActiviteAt = derniereActiviteAt; }

    public Integer getCandidatsCount() { return candidatsCount; }
    public void setCandidatsCount(Integer candidatsCount) { this.candidatsCount = candidatsCount; }

    public Set<String> getBadges() { return badges; }
    public void setBadges(Set<String> badges) { this.badges = badges; }

    public List<String> getRaisonsMatching() { return raisonsMatching; }
    public void setRaisonsMatching(List<String> raisonsMatching) { this.raisonsMatching = raisonsMatching; }

    /* ======== Clôture / policy ======== */
    private ClosurePolicy closurePolicy;
    private boolean closedByClient;
    private boolean closedByFreelancer;
    private BigDecimal contractTotalAmount;

    public ClosurePolicy getClosurePolicy() { return closurePolicy; }
    public void setClosurePolicy(ClosurePolicy closurePolicy) { this.closurePolicy = closurePolicy; }
    public boolean isClosedByClient() { return closedByClient; }
    public void setClosedByClient(boolean closedByClient) { this.closedByClient = closedByClient; }
    public boolean isClosedByFreelancer() { return closedByFreelancer; }
    public void setClosedByFreelancer(boolean closedByFreelancer) { this.closedByFreelancer = closedByFreelancer; }
    public BigDecimal getContractTotalAmount() { return contractTotalAmount; }
    public void setContractTotalAmount(BigDecimal contractTotalAmount) { this.contractTotalAmount = contractTotalAmount; }
}

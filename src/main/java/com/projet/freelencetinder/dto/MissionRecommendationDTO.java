package com.projet.freelencetinder.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.projet.freelencetinder.models.Mission.Categorie;
import com.projet.freelencetinder.models.Mission.Gouvernorat;
import com.projet.freelencetinder.models.Mission.ModaliteTravail;
import com.projet.freelencetinder.models.Mission.Statut;
import com.projet.freelencetinder.models.Mission.TypeRemuneration;
import com.projet.freelencetinder.models.Utilisateur.Langue;
import com.projet.freelencetinder.models.Utilisateur.NiveauExperience;
import com.projet.freelencetinder.models.Utilisateur.NiveauLangue;

public class MissionRecommendationDTO {

    private Long id;
    private String titre;

    private BigDecimal budget;
    private String devise;

    private Categorie categorie;
    private Statut statut;
    private ModaliteTravail modaliteTravail;

    private LocalDateTime datePublication;
    private LocalDate dateLimiteCandidature;
    private Integer dureeEstimeeJours;

    private ClientInfoDTO client;

    /* Matching */
    private int score;               // Score brut (tri)
    private int matchedSkills;
    private int totalRequiredSkills;
    private double matchRatio;
    private boolean urgent;
    private boolean expired;

    /* Swipe state */
    private boolean alreadySwiped;
    private boolean likedByCurrentUser;
    private boolean mutualMatch;

    /* ======== AJOUTS ======== */
    private TypeRemuneration typeRemuneration;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private BigDecimal tjmJournalier;

    private Gouvernorat gouvernorat;
    private Integer chargeHebdoJours;
    private LocalDate dateDebutSouhaitee;

    private NiveauExperience niveauExperienceMin;
    private Map<Langue, NiveauLangue> languesRequises;

    private Double scoreMatching;        // score calculé côté back
    private List<String> raisonsMatching;

    public MissionRecommendationDTO() {}

    /* Getters / Setters (existants + ajouts) */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public ModaliteTravail getModaliteTravail() { return modaliteTravail; }
    public void setModaliteTravail(ModaliteTravail modaliteTravail) { this.modaliteTravail = modaliteTravail; }

    public LocalDateTime getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDateTime datePublication) { this.datePublication = datePublication; }

    public LocalDate getDateLimiteCandidature() { return dateLimiteCandidature; }
    public void setDateLimiteCandidature(LocalDate dateLimiteCandidature) { this.dateLimiteCandidature = dateLimiteCandidature; }

    public Integer getDureeEstimeeJours() { return dureeEstimeeJours; }
    public void setDureeEstimeeJours(Integer dureeEstimeeJours) { this.dureeEstimeeJours = dureeEstimeeJours; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(int matchedSkills) { this.matchedSkills = matchedSkills; }

    public int getTotalRequiredSkills() { return totalRequiredSkills; }
    public void setTotalRequiredSkills(int totalRequiredSkills) { this.totalRequiredSkills = totalRequiredSkills; }

    public double getMatchRatio() { return matchRatio; }
    public void setMatchRatio(double matchRatio) { this.matchRatio = matchRatio; }

    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }

    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }

    public boolean isAlreadySwiped() { return alreadySwiped; }
    public void setAlreadySwiped(boolean alreadySwiped) { this.alreadySwiped = alreadySwiped; }

    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
    public void setLikedByCurrentUser(boolean likedByCurrentUser) { this.likedByCurrentUser = likedByCurrentUser; }

    public boolean isMutualMatch() { return mutualMatch; }
    public void setMutualMatch(boolean mutualMatch) { this.mutualMatch = mutualMatch; }

    public ClientInfoDTO getClient() { return client; }
    public void setClient(ClientInfoDTO client) { this.client = client; }

    // AJOUTS
    public TypeRemuneration getTypeRemuneration() { return typeRemuneration; }
    public void setTypeRemuneration(TypeRemuneration typeRemuneration) { this.typeRemuneration = typeRemuneration; }

    public BigDecimal getBudgetMin() { return budgetMin; }
    public void setBudgetMin(BigDecimal budgetMin) { this.budgetMin = budgetMin; }

    public BigDecimal getBudgetMax() { return budgetMax; }
    public void setBudgetMax(BigDecimal budgetMax) { this.budgetMax = budgetMax; }

    public BigDecimal getTjmJournalier() { return tjmJournalier; }
    public void setTjmJournalier(BigDecimal tjmJournalier) { this.tjmJournalier = tjmJournalier; }

    public Gouvernorat getGouvernorat() { return gouvernorat; }
    public void setGouvernorat(Gouvernorat gouvernorat) { this.gouvernorat = gouvernorat; }

    public Integer getChargeHebdoJours() { return chargeHebdoJours; }
    public void setChargeHebdoJours(Integer chargeHebdoJours) { this.chargeHebdoJours = chargeHebdoJours; }

    public LocalDate getDateDebutSouhaitee() { return dateDebutSouhaitee; }
    public void setDateDebutSouhaitee(LocalDate dateDebutSouhaitee) { this.dateDebutSouhaitee = dateDebutSouhaitee; }

    public NiveauExperience getNiveauExperienceMin() { return niveauExperienceMin; }
    public void setNiveauExperienceMin(NiveauExperience niveauExperienceMin) { this.niveauExperienceMin = niveauExperienceMin; }

    public Map<Langue, NiveauLangue> getLanguesRequises() { return languesRequises; }
    public void setLanguesRequises(Map<Langue, NiveauLangue> languesRequises) { this.languesRequises = languesRequises; }

    public Double getScoreMatching() { return scoreMatching; }
    public void setScoreMatching(Double scoreMatching) { this.scoreMatching = scoreMatching; }

    public List<String> getRaisonsMatching() { return raisonsMatching; }
    public void setRaisonsMatching(List<String> raisonsMatching) { this.raisonsMatching = raisonsMatching; }
}

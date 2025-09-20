package com.projet.freelencetinder.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.projet.freelencetinder.models.Mission.Categorie;
import com.projet.freelencetinder.models.Mission.Gouvernorat;
import com.projet.freelencetinder.models.Mission.ModaliteTravail;
import com.projet.freelencetinder.models.Mission.NiveauBrief;
import com.projet.freelencetinder.models.Mission.Statut;
import com.projet.freelencetinder.models.Mission.TypeRemuneration;
import com.projet.freelencetinder.models.Utilisateur.NiveauExperience;
import com.projet.freelencetinder.models.Mission.ClosurePolicy;

public class MissionCardDto {
    private Long id;
    private String titre;
    private String description;
    private BigDecimal budget;
    private String devise;
    private Statut statut;
    private FreelanceSummaryDTO freelance; // CORRECTION : utiliser le bon DTO

    /* ======== AJOUTS ======== */
    private Categorie categorie;
    private ModaliteTravail modaliteTravail;

    private TypeRemuneration typeRemuneration;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private BigDecimal tjmJournalier;

    private Gouvernorat gouvernorat;
    private LocalDate dateLimiteCandidature;
    private LocalDate dateDebutSouhaitee;
    private Integer chargeHebdoJours;

    private boolean urgent;
    private NiveauBrief qualiteBrief;
    private NiveauExperience niveauExperienceMin;

    private Double scoreMatching;
    private List<String> matchReasons;
    private Integer candidatsCount;
    private Set<String> badges;

    private LocalDateTime derniereActiviteAt;

    private ClientInfoDTO client; // exposer le client sur la carte

    // ======== Enrichissements opérationnels pour la carte ========
    private java.time.LocalDate delaiLivraison;

    private Integer livrablesTotal;
    private Integer livrablesValides;
    private Integer livrablesEnAttente;
    private Integer progressPct;           // 0..100
    private Long    livrableIdEnAttente;   // pour ouvrir direct le bon item

    private Boolean trancheDue;
    private Long    trancheIdDue;

    public enum NextAction { VALIDER_LIVRABLE, PAYER_TRANCHE, BOOSTER, DETAILS }
    private NextAction nextAction;

    /* ======== Clôture / policy ======== */
    private ClosurePolicy closurePolicy;
    private boolean pretACloturer;
    private boolean closedByClient;
    private boolean closedByFreelancer;
    private BigDecimal contractTotalAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public FreelanceSummaryDTO getFreelance() { return freelance; }
    public void setFreelance(FreelanceSummaryDTO freelance) { this.freelance = freelance; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public ModaliteTravail getModaliteTravail() { return modaliteTravail; }
    public void setModaliteTravail(ModaliteTravail modaliteTravail) { this.modaliteTravail = modaliteTravail; }

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

    public LocalDate getDateLimiteCandidature() { return dateLimiteCandidature; }
    public void setDateLimiteCandidature(LocalDate dateLimiteCandidature) { this.dateLimiteCandidature = dateLimiteCandidature; }

    public LocalDate getDateDebutSouhaitee() { return dateDebutSouhaitee; }
    public void setDateDebutSouhaitee(LocalDate dateDebutSouhaitee) { this.dateDebutSouhaitee = dateDebutSouhaitee; }

    public Integer getChargeHebdoJours() { return chargeHebdoJours; }
    public void setChargeHebdoJours(Integer chargeHebdoJours) { this.chargeHebdoJours = chargeHebdoJours; }

    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }

    public NiveauBrief getQualiteBrief() { return qualiteBrief; }
    public void setQualiteBrief(NiveauBrief qualiteBrief) { this.qualiteBrief = qualiteBrief; }

    public NiveauExperience getNiveauExperienceMin() { return niveauExperienceMin; }
    public void setNiveauExperienceMin(NiveauExperience niveauExperienceMin) { this.niveauExperienceMin = niveauExperienceMin; }

    public Double getScoreMatching() { return scoreMatching; }
    public void setScoreMatching(Double scoreMatching) { this.scoreMatching = scoreMatching; }

    public List<String> getMatchReasons() { return matchReasons; }
    public void setMatchReasons(List<String> matchReasons) { this.matchReasons = matchReasons; }

    public Integer getCandidatsCount() { return candidatsCount; }
    public void setCandidatsCount(Integer candidatsCount) { this.candidatsCount = candidatsCount; }

    public Set<String> getBadges() { return badges; }
    public void setBadges(Set<String> badges) { this.badges = badges; }

    public LocalDateTime getDerniereActiviteAt() { return derniereActiviteAt; }
    public void setDerniereActiviteAt(LocalDateTime derniereActiviteAt) { this.derniereActiviteAt = derniereActiviteAt; }

    public ClientInfoDTO getClient() { return client; }
    public void setClient(ClientInfoDTO client) { this.client = client; }

    public java.time.LocalDate getDelaiLivraison() { return delaiLivraison; }
    public void setDelaiLivraison(java.time.LocalDate delaiLivraison) { this.delaiLivraison = delaiLivraison; }

    public Integer getLivrablesTotal() { return livrablesTotal; }
    public void setLivrablesTotal(Integer livrablesTotal) { this.livrablesTotal = livrablesTotal; }

    public Integer getLivrablesValides() { return livrablesValides; }
    public void setLivrablesValides(Integer livrablesValides) { this.livrablesValides = livrablesValides; }

    public Integer getLivrablesEnAttente() { return livrablesEnAttente; }
    public void setLivrablesEnAttente(Integer livrablesEnAttente) { this.livrablesEnAttente = livrablesEnAttente; }

    public Integer getProgressPct() { return progressPct; }
    public void setProgressPct(Integer progressPct) { this.progressPct = progressPct; }

    public Long getLivrableIdEnAttente() { return livrableIdEnAttente; }
    public void setLivrableIdEnAttente(Long livrableIdEnAttente) { this.livrableIdEnAttente = livrableIdEnAttente; }

    public Boolean getTrancheDue() { return trancheDue; }
    public void setTrancheDue(Boolean trancheDue) { this.trancheDue = trancheDue; }

    public Long getTrancheIdDue() { return trancheIdDue; }
    public void setTrancheIdDue(Long trancheIdDue) { this.trancheIdDue = trancheIdDue; }

    public NextAction getNextAction() { return nextAction; }
    public void setNextAction(NextAction nextAction) { this.nextAction = nextAction; }

    public ClosurePolicy getClosurePolicy() { return closurePolicy; }
    public void setClosurePolicy(ClosurePolicy closurePolicy) { this.closurePolicy = closurePolicy; }
    public boolean isPretACloturer() { return pretACloturer; }
    public void setPretACloturer(boolean pretACloturer) { this.pretACloturer = pretACloturer; }
    public boolean isClosedByClient() { return closedByClient; }
    public void setClosedByClient(boolean closedByClient) { this.closedByClient = closedByClient; }
    public boolean isClosedByFreelancer() { return closedByFreelancer; }
    public void setClosedByFreelancer(boolean closedByFreelancer) { this.closedByFreelancer = closedByFreelancer; }
    public BigDecimal getContractTotalAmount() { return contractTotalAmount; }
    public void setContractTotalAmount(BigDecimal contractTotalAmount) { this.contractTotalAmount = contractTotalAmount; }
}

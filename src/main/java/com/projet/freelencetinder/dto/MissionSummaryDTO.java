package com.projet.freelencetinder.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import com.projet.freelencetinder.models.Mission.Categorie;
import com.projet.freelencetinder.models.Mission.Gouvernorat;
import com.projet.freelencetinder.models.Mission.ModaliteTravail;
import com.projet.freelencetinder.models.Mission.Statut;
import com.projet.freelencetinder.models.Mission.TypeRemuneration;
import com.projet.freelencetinder.models.Mission.NiveauBrief;
import com.projet.freelencetinder.models.Utilisateur.NiveauExperience;
import com.projet.freelencetinder.models.Mission.ClosurePolicy;

public class MissionSummaryDTO {

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

    private boolean urgent;
    private boolean expired;

    private ClientInfoDTO client;

    /* ======== AJOUTS ======== */
    private TypeRemuneration typeRemuneration;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private BigDecimal tjmJournalier;

    private Gouvernorat gouvernorat;
    private Integer chargeHebdoJours;
    private LocalDate dateDebutSouhaitee;

    private NiveauBrief qualiteBrief;
    private NiveauExperience niveauExperienceMin;

    private LocalDateTime derniereActiviteAt;
    private Integer candidatsCount;
    private Set<String> badges;

    /* ======== Clôture / policy (léger) ======== */
    private ClosurePolicy closurePolicy;
    private boolean pretACloturer;
    private BigDecimal contractTotalAmount;

    public MissionSummaryDTO() {}

    // Getters & Setters (existants)
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

    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }

    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }

    public ClientInfoDTO getClient() { return client; }
    public void setClient(ClientInfoDTO client) { this.client = client; }

    // Getters & Setters AJOUTS
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

    public NiveauBrief getQualiteBrief() { return qualiteBrief; }
    public void setQualiteBrief(NiveauBrief qualiteBrief) { this.qualiteBrief = qualiteBrief; }

    public NiveauExperience getNiveauExperienceMin() { return niveauExperienceMin; }
    public void setNiveauExperienceMin(NiveauExperience niveauExperienceMin) { this.niveauExperienceMin = niveauExperienceMin; }

    public LocalDateTime getDerniereActiviteAt() { return derniereActiviteAt; }
    public void setDerniereActiviteAt(LocalDateTime derniereActiviteAt) { this.derniereActiviteAt = derniereActiviteAt; }

    public Integer getCandidatsCount() { return candidatsCount; }
    public void setCandidatsCount(Integer candidatsCount) { this.candidatsCount = candidatsCount; }

    public Set<String> getBadges() { return badges; }
    public void setBadges(Set<String> badges) { this.badges = badges; }

    public ClosurePolicy getClosurePolicy() { return closurePolicy; }
    public void setClosurePolicy(ClosurePolicy closurePolicy) { this.closurePolicy = closurePolicy; }
    public boolean isPretACloturer() { return pretACloturer; }
    public void setPretACloturer(boolean pretACloturer) { this.pretACloturer = pretACloturer; }
    public BigDecimal getContractTotalAmount() { return contractTotalAmount; }
    public void setContractTotalAmount(BigDecimal contractTotalAmount) { this.contractTotalAmount = contractTotalAmount; }
}

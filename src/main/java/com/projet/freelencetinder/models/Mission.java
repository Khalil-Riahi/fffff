package com.projet.freelencetinder.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Mission publiée par un client et potentiellement swipée par des freelances.
 */
@Entity
@Table(
    name = "mission",
    indexes = {
        @Index(name = "idx_mission_statut", columnList = "statut"),
        @Index(name = "idx_mission_cat_statut", columnList = "categorie, statut"),
        @Index(name = "idx_mission_date_pub", columnList = "datePublication"),
        /* ===================== AJOUTS : index utiles pour swipe/filtrage ===================== */
        @Index(name = "idx_mission_modalite", columnList = "modaliteTravail"),
        @Index(name = "idx_mission_limite", columnList = "dateLimiteCandidature"),
        @Index(name = "idx_mission_remuneration", columnList = "typeRemuneration"),
        @Index(name = "idx_mission_gouvernorat", columnList = "gouvernorat")
    }
)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Mission {

    /* ===================== Identité & Version ===================== */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    /* ===================== Métadonnées ===================== */
    @NotBlank @Size(max = 180)
    private String titre;

    @NotBlank @Size(max = 4000)
    @Column(length = 4000, nullable = false)
    private String description;

    @ElementCollection
    @CollectionTable(name = "mission_competences_requises", joinColumns = @JoinColumn(name = "mission_id"))
    @Column(name = "competence", length = 120)
    private Set<String> competencesRequises = new HashSet<>();

    /* ===================== Ajouts: priorisation compétences / langues / séniorité ===================== */
    // AJOUT - Priorité des compétences (MUST / NICE)
    @ElementCollection
    @CollectionTable(name = "mission_competences_priorite", joinColumns = @JoinColumn(name = "mission_id"))
    @MapKeyColumn(name = "competence", length = 120)
    @Column(name = "importance", length = 10)
    @Enumerated(EnumType.STRING)
    private Map<String, Importance> competencesPriorisees = new HashMap<>();

    // AJOUT - Langues requises (FR/AR/EN) avec niveau (A1..C2/NATIF) — Tunisie
    @ElementCollection
    @CollectionTable(name = "mission_langues_requises", joinColumns = @JoinColumn(name = "mission_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "niveau", length = 10)
    @Enumerated(EnumType.STRING)
    private Map<Utilisateur.Langue, Utilisateur.NiveauLangue> languesRequises = new HashMap<>();

    // AJOUT - Séniorité minimale souhaitée
    @Enumerated(EnumType.STRING)
    private Utilisateur.NiveauExperience niveauExperienceMin;

    /* ===================== Budget / rémunération (sans escrow) ===================== */
    @NotNull
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal budget;

    @Size(max = 10)
    private String devise = "TND";

    // AJOUT - Type de rémunération (FORFAIT ou TJM)
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private TypeRemuneration typeRemuneration = TypeRemuneration.FORFAIT;

    // AJOUT - Fourchette budget (facultatif) pour affichage carte
    @Column(precision = 14, scale = 2)
    private BigDecimal budgetMin;

    @Column(precision = 14, scale = 2)
    private BigDecimal budgetMax;

    // AJOUT - TJM journalier si typeRemuneration = TJM
    @Column(precision = 14, scale = 2)
    private BigDecimal tjmJournalier;

    /* ===================== Planning / charge ===================== */
    @NotNull
    private LocalDate delaiLivraison;

    private Integer dureeEstimeeJours;
    private LocalDate dateLimiteCandidature;

    // AJOUT - Date de démarrage souhaitée (utile pour tri/signal “urgent”)
    private LocalDate dateDebutSouhaitee;

    // AJOUT - Charge hebdomadaire (jours/sem)
    @Min(0) @Max(7)
    private Integer chargeHebdoJours;

    @Size(max = 160)
    private String localisation;

    // AJOUT - Gouvernorat (Tunisie) pour filtrage local
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Gouvernorat gouvernorat;

    @Enumerated(EnumType.STRING)
    private ModaliteTravail modaliteTravail = ModaliteTravail.NON_SPECIFIE;

    // AJOUT - Indicateur d’urgence + qualité du brief (pour UX carte)
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean urgent = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    private NiveauBrief qualiteBrief;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private Statut statut = Statut.EN_ATTENTE;

    /* ===================== Clôture & politique ===================== */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClosurePolicy closurePolicy = ClosurePolicy.FINAL_MILESTONE_REQUIRED;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean closedByClient = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean closedByFreelancer = false;

    @Column(precision = 14, scale = 2)
    private BigDecimal contractTotalAmount;

    /* ===================== Relations ===================== */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Utilisateur client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelance_selectionne_id")
    private Utilisateur freelanceSelectionne;

    /* Mission.java : ajouter */
    @OneToMany(mappedBy = "mission")
    @JsonIgnore           // évite boucle JSON
    private List<TranchePaiement> tranches = new ArrayList<>();

    // Mission.java
    @OneToMany(mappedBy = "mission")
    @JsonIgnore
    private List<Livrable> livrables = new ArrayList<>();

    /* ===================== Dates / audit ===================== */
    @Column(nullable = false, updatable = false)
    private LocalDateTime datePublication;

    private LocalDateTime dateAffectation;
    private LocalDateTime dateDerniereMiseAJour;

    // AJOUT - Dernière activité côté client (mise à jour / réponse)
    private LocalDateTime derniereActiviteAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Categorie categorie;

    /* ===================== Médias ===================== */
    @ElementCollection
    @CollectionTable(name = "mission_media", joinColumns = @JoinColumn(name = "mission_id"))
    @Column(name = "media_url", length = 600)
    private List<String> mediaUrls = new ArrayList<>();

    @Size(max = 600)
    private String videoBriefUrl;

    /* ===================== Match & Swipes ===================== */
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer swipesRecus = 0;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer likesRecus = 0;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean verrouillee = false;

    @Transient
    private Double scoreMatching;

    // AJOUT - Raisons de matching (pour popover “Pourquoi ce match ?”)
    @Transient
    private List<String> raisonsMatching;

    // AJOUT - Nombre de candidats déjà en lice
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer candidatsCount = 0;

    // AJOUT - Badges/Tags mission (ex: URGENT, STARTUP, NDA, BILINGUE)
    @ElementCollection
    @CollectionTable(name = "mission_badges", joinColumns = @JoinColumn(name = "mission_id"))
    @Column(name = "badge", length = 40)
    private Set<String> badges = new HashSet<>();

    // AJOUT - Taille équipe côté client & présence Tech Lead (info rassurante)
    private Integer tailleEquipeClient;
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean presenceTechLead = false;

    /* ===================== Hooks ===================== */
    @PrePersist
    protected void onCreate() {
        this.datePublication = LocalDateTime.now();
        this.dateDerniereMiseAJour = this.datePublication;
        if (budget == null) budget = BigDecimal.ZERO;
        if (devise == null) devise = "TND";
        if (swipesRecus == null) swipesRecus = 0;
        if (likesRecus == null) likesRecus = 0;
        // verrouillee déjà false par défaut
        if (candidatsCount == null) candidatsCount = 0; // AJOUT
        if (closurePolicy == null) closurePolicy = ClosurePolicy.FINAL_MILESTONE_REQUIRED;
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateDerniereMiseAJour = LocalDateTime.now();
        this.derniereActiviteAt = this.dateDerniereMiseAJour; // AJOUT
    }

    /* ===================== Méthodes utilitaires ===================== */
    public boolean estExpirée() {
        return dateLimiteCandidature != null &&
               LocalDate.now().isAfter(dateLimiteCandidature);
    }

    public void incrementSwipe() {
        if (swipesRecus == null) swipesRecus = 0;
        swipesRecus++;
    }

    public void incrementLike() {
        if (likesRecus == null) likesRecus = 0;
        likesRecus++;
    }

    public void affecterFreelance(Utilisateur f) {
        this.freelanceSelectionne = f;
        this.dateAffectation = LocalDateTime.now();
        this.statut = Statut.EN_COURS;
        this.verrouillee = true;
    }

    public boolean estDisponiblePourSwipe() {
        return statut == Statut.EN_ATTENTE && !verrouillee && !estExpirée();
    }

    /* ===================== Getters / Setters ===================== */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<String> getCompetencesRequises() { return competencesRequises; }
    public void setCompetencesRequises(Set<String> competencesRequises) { this.competencesRequises = competencesRequises; }

    public Map<String, Importance> getCompetencesPriorisees() { return competencesPriorisees; }
    public void setCompetencesPriorisees(Map<String, Importance> competencesPriorisees) { this.competencesPriorisees = competencesPriorisees; }

    public Map<Utilisateur.Langue, Utilisateur.NiveauLangue> getLanguesRequises() { return languesRequises; }
    public void setLanguesRequises(Map<Utilisateur.Langue, Utilisateur.NiveauLangue> languesRequises) { this.languesRequises = languesRequises; }

    public Utilisateur.NiveauExperience getNiveauExperienceMin() { return niveauExperienceMin; }
    public void setNiveauExperienceMin(Utilisateur.NiveauExperience niveauExperienceMin) { this.niveauExperienceMin = niveauExperienceMin; }

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

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public ClosurePolicy getClosurePolicy() { return closurePolicy; }
    public void setClosurePolicy(ClosurePolicy closurePolicy) { this.closurePolicy = closurePolicy; }

    public boolean isClosedByClient() { return closedByClient; }
    public void setClosedByClient(boolean closedByClient) { this.closedByClient = closedByClient; }

    public boolean isClosedByFreelancer() { return closedByFreelancer; }
    public void setClosedByFreelancer(boolean closedByFreelancer) { this.closedByFreelancer = closedByFreelancer; }

    public BigDecimal getContractTotalAmount() { return contractTotalAmount; }
    public void setContractTotalAmount(BigDecimal contractTotalAmount) { this.contractTotalAmount = contractTotalAmount; }

    public Utilisateur getClient() { return client; }
    public void setClient(Utilisateur client) { this.client = client; }

    public Utilisateur getFreelanceSelectionne() { return freelanceSelectionne; }
    public void setFreelanceSelectionne(Utilisateur freelanceSelectionne) { this.freelanceSelectionne = freelanceSelectionne; }

    public List<TranchePaiement> getTranches() { return tranches; }
    public void setTranches(List<TranchePaiement> tranches) { this.tranches = tranches; }

    public List<Livrable> getLivrables() { return livrables; }
    public void setLivrables(List<Livrable> livrables) { this.livrables = livrables; }

    public LocalDateTime getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDateTime datePublication) { this.datePublication = datePublication; }

    public LocalDateTime getDateAffectation() { return dateAffectation; }
    public void setDateAffectation(LocalDateTime dateAffectation) { this.dateAffectation = dateAffectation; }

    public LocalDateTime getDateDerniereMiseAJour() { return dateDerniereMiseAJour; }
    public void setDateDerniereMiseAJour(LocalDateTime dateDerniereMiseAJour) { this.dateDerniereMiseAJour = dateDerniereMiseAJour; }

    public LocalDateTime getDerniereActiviteAt() { return derniereActiviteAt; }
    public void setDerniereActiviteAt(LocalDateTime derniereActiviteAt) { this.derniereActiviteAt = derniereActiviteAt; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public List<String> getMediaUrls() { return mediaUrls; }
    public void setMediaUrls(List<String> mediaUrls) { this.mediaUrls = mediaUrls; }

    public String getVideoBriefUrl() { return videoBriefUrl; }
    public void setVideoBriefUrl(String videoBriefUrl) { this.videoBriefUrl = videoBriefUrl; }

    public Integer getSwipesRecus() { return swipesRecus; }
    public void setSwipesRecus(Integer swipesRecus) { this.swipesRecus = swipesRecus; }

    public Integer getLikesRecus() { return likesRecus; }
    public void setLikesRecus(Integer likesRecus) { this.likesRecus = likesRecus; }

    public boolean isVerrouillee() { return verrouillee; }
    public void setVerrouillee(boolean verrouillee) { this.verrouillee = verrouillee; }

    public Double getScoreMatching() { return scoreMatching; }
    public void setScoreMatching(Double scoreMatching) { this.scoreMatching = scoreMatching; }

    public List<String> getRaisonsMatching() { return raisonsMatching; }
    public void setRaisonsMatching(List<String> raisonsMatching) { this.raisonsMatching = raisonsMatching; }

    public Integer getCandidatsCount() { return candidatsCount; }
    public void setCandidatsCount(Integer candidatsCount) { this.candidatsCount = candidatsCount; }

    public Set<String> getBadges() { return badges; }
    public void setBadges(Set<String> badges) { this.badges = badges; }

    public Integer getTailleEquipeClient() { return tailleEquipeClient; }
    public void setTailleEquipeClient(Integer tailleEquipeClient) { this.tailleEquipeClient = tailleEquipeClient; }

    public boolean isPresenceTechLead() { return presenceTechLead; }
    public void setPresenceTechLead(boolean presenceTechLead) { this.presenceTechLead = presenceTechLead; }

    /* ===================== Enums ===================== */
    public enum Statut {
        EN_ATTENTE, EN_COURS, EN_ATTENTE_VALIDATION, PRET_A_CLOTURER, TERMINEE, ANNULEE, EXPIREE
    }

    public enum Categorie {
        DEVELOPPEMENT_WEB, DEVELOPPEMENT_MOBILE, DESIGN_GRAPHIQUE, REDACTION_CONTENU,
        MARKETING_DIGITAL, VIDEO_MONTAGE, TRADUCTION, SUPPORT_TECHNIQUE, CONSULTING, AUTRE
    }

    public enum ModaliteTravail { DISTANCIEL, PRESENTIEL, HYBRIDE, NON_SPECIFIE }

    /* ===================== Enums AJOUT ===================== */
    public enum TypeRemuneration { FORFAIT, TJM }

    public enum Importance { MUST, NICE }

    public enum NiveauBrief { COMPLET, MOYEN, LACUNAIRE }

    /** Gouvernorats de Tunisie (pour filtrage local précis) */
    public enum Gouvernorat {
        TUNIS, ARIANA, BEN_AROUS, MANOUBA, NABEUL, BIZERTE, BEJA, JENDOUBA, ZAGHOUAN,
        SILIANA, KEF, SOUSSE, MONASTIR, MAHDIA, KAIROUAN, KASSERINE, SIDI_BOUZID,
        SFAX, GABES, MEDENINE, TATAOUINE, GAFSA, TOZEUR, KEBILI
    }

    public enum ClosurePolicy {
        FINAL_MILESTONE_REQUIRED,
        MANUAL_DUAL_CONFIRM,
        CONTRACT_TOTAL_AMOUNT
    }
}

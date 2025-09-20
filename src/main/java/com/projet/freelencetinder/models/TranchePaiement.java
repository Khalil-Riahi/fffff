package com.projet.freelencetinder.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.projet.freelencetinder.models.StatusLivrable;

/**
 * Tranche (milestone) de paiement.
 * MVP (Flouci/D17) : paiement direct (pas d’escrow), commission plateforme = 0.
 * Phase 2 (Paymee) : escrow + commission possible (réactiver COMMISSION_RATE > 0).
 */
@Entity
@Table(
    name = "tranche_paiement",
    indexes = {
        @Index(name = "idx_tranche_statut", columnList = "statut"),
        @Index(name = "idx_tranche_mission", columnList = "mission_id"),
        @Index(name = "idx_tranche_mission_statut_ordre", columnList = "mission_id, statut, ordre")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_tranche_livrable", columnNames = {"livrable_id"})
    }
)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TranchePaiement {

    /* ---------- Constantes ---------- */
    /** Commission plateforme (MVP direct = 0 %). Mettre >0 (ex: 0.07) en Phase 2. */
    public static final BigDecimal COMMISSION_RATE = BigDecimal.ZERO;

    /* ---------- Identité ---------- */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    /* ---------- Métadonnées ---------- */
    @NotNull @Min(1)
    private Integer ordre;

    @NotBlank @Size(max = 160)
    private String titre;

    @DecimalMin("0.0")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montantBrut;

    /** Commission calculée (= montantBrut × COMMISSION_RATE). */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal commissionPlateforme;

    /** Montant réellement versé au freelance (= montantBrut − commission). */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montantNetFreelance;

    @Size(max = 10)
    private String devise = "TND";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutTranche statut = StatutTranche.EN_ATTENTE_DEPOT;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean required = true;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean finale = false;

    /* ---------- Dates ---------- */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateDepot;        // génération lien paiement
    private LocalDateTime dateValidation;   // (utilisé en mode escrow)
    private LocalDateTime dateVersement;    // webhook PAID

    /* ---------- Champs de provider (réutilisation générique) ---------- */
    @Size(max = 128)     // augmenté (tokens parfois longs)
    private String paymeeCheckoutId;   // utilisé aussi pour stocker le token Flouci

    @Size(max = 1000)    // augmenté (URLs parfois longues)
    private String paymeePaymentUrl;   // utilisé aussi pour stocker l’URL Flouci

    /* ---------- Relations ---------- */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Utilisateur client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "freelance_id", nullable = false)
    private Utilisateur freelance;

    /* ---------- Lien avec un livrable (optionnel) ---------- */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livrable_id")
    private Livrable livrableAssocie;

    /* ---------- Hooks ---------- */
    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
        calculerCommissions();
    }

    @PreUpdate
    protected void onUpdate() {
        calculerCommissions();
    }

    private void calculerCommissions() {
        if (montantBrut == null) return;
        commissionPlateforme = montantBrut
                .multiply(COMMISSION_RATE)
                .setScale(2, RoundingMode.HALF_UP);
        montantNetFreelance = montantBrut.subtract(commissionPlateforme);
    }

    /* ---------- Méthodes métier ---------- */
    public void marquerDepotEffectue(String checkoutToken) {
        this.statut = StatutTranche.EN_ATTENTE_PAIEMENT;
        this.dateDepot = LocalDateTime.now();
        this.paymeeCheckoutId = checkoutToken;
    }

    public void marquerLivrableValide() {
        this.statut = StatutTranche.VALIDEE;
        this.dateValidation = LocalDateTime.now();
    }

    public void marquerVersementEffectue() {
        this.statut = StatutTranche.VERSEE_FREELANCE;
        this.dateVersement = LocalDateTime.now();
    }

    /* ---------- Getters / Setters ---------- */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public BigDecimal getMontantBrut() { return montantBrut; }
    public void setMontantBrut(BigDecimal montantBrut) { this.montantBrut = montantBrut; }
    public BigDecimal getCommissionPlateforme() { return commissionPlateforme; }
    public void setCommissionPlateforme(BigDecimal commissionPlateforme) { this.commissionPlateforme = commissionPlateforme; }
    public BigDecimal getMontantNetFreelance() { return montantNetFreelance; }
    public void setMontantNetFreelance(BigDecimal montantNetFreelance) { this.montantNetFreelance = montantNetFreelance; }
    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }
    public StatutTranche getStatut() { return statut; }
    public void setStatut(StatutTranche statut) { this.statut = statut; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public LocalDateTime getDateDepot() { return dateDepot; }
    public void setDateDepot(LocalDateTime dateDepot) { this.dateDepot = dateDepot; }
    public LocalDateTime getDateValidation() { return dateValidation; }
    public void setDateValidation(LocalDateTime dateValidation) { this.dateValidation = dateValidation; }
    public LocalDateTime getDateVersement() { return dateVersement; }
    public void setDateVersement(LocalDateTime dateVersement) { this.dateVersement = dateVersement; }
    public String getPaymeeCheckoutId() { return paymeeCheckoutId; }
    public void setPaymeeCheckoutId(String paymeeCheckoutId) { this.paymeeCheckoutId = paymeeCheckoutId; }
    public String getPaymeePaymentUrl() { return paymeePaymentUrl; }
    public void setPaymeePaymentUrl(String paymeePaymentUrl) { this.paymeePaymentUrl = paymeePaymentUrl; }
    public Mission getMission() { return mission; }
    public void setMission(Mission mission) { this.mission = mission; }
    public Utilisateur getClient() { return client; }
    public void setClient(Utilisateur client) { this.client = client; }
    public Utilisateur getFreelance() { return freelance; }
    public void setFreelance(Utilisateur freelance) { this.freelance = freelance; }
    public Livrable getLivrableAssocie() { return livrableAssocie; }
    public void setLivrableAssocie(Livrable livrableAssocie) { this.livrableAssocie = livrableAssocie; }

    /* ---------- Helpers lecture ---------- */
    @Transient
    public boolean isPaid() {
        return this.statut == StatutTranche.VERSEE_FREELANCE;
    }

    @Transient
    public boolean isDeliveryAccepted() {
        return this.livrableAssocie != null &&
               this.livrableAssocie.getStatus() == StatusLivrable.VALIDE;
    }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public boolean isFinale() { return finale; }
    public void setFinale(boolean finale) { this.finale = finale; }

    public enum StatutTranche {
        EN_ATTENTE_DEPOT,
        EN_ATTENTE_PAIEMENT,
        FONDS_BLOQUES,            // non utilisé en MVP direct
        EN_ATTENTE_VALIDATION,    // utilisé en mode escrow
        VALIDEE,
        VERSEE_FREELANCE,
        REJETEE,
        ERREUR_CAPTURE            // non utilisé en MVP direct
    }
}

package com.projet.freelencetinder.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "withdrawal_request")
public class WithdrawalRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Devise devise = Devise.TND;

    public enum Statut { EN_ATTENTE, PAYE, ERREUR }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Statut statut = Statut.EN_ATTENTE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateDemande;

    private LocalDateTime datePaiement;

    @Column(length = 120)
    private String paymeeReference;

    /* ---------- Relations ---------- */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "freelance_id", nullable = false)
    private Utilisateur freelance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "method_id", nullable = false)
    private WithdrawalMethod method;

    /* ---------- Hooks ---------- */
    @PrePersist
    protected void onCreate() {
        this.dateDemande = LocalDateTime.now();
    }

    /* ---------- Getters / Setters ---------- */
    public Long getId() { return id; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public Devise getDevise() { return devise; }
    public void setDevise(Devise devise) { this.devise = devise; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public LocalDateTime getDateDemande() { return dateDemande; }

    public LocalDateTime getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDateTime datePaiement) { this.datePaiement = datePaiement; }

    public String getPaymeeReference() { return paymeeReference; }
    public void setPaymeeReference(String paymeeReference) { this.paymeeReference = paymeeReference; }

    public Utilisateur getFreelance() { return freelance; }
    public void setFreelance(Utilisateur freelance) { this.freelance = freelance; }

    public WithdrawalMethod getMethod() { return method; }
    public void setMethod(WithdrawalMethod method) { this.method = method; }
}

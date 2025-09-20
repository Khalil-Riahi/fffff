package com.projet.freelencetinder.dto.paiement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WithdrawalRequestResponseDTO {

    private Long id;
    private BigDecimal montant;
    private String devise;
    private String statut;
    private LocalDateTime dateDemande;
    private LocalDateTime datePaiement;
    private String paymeeReference;

    /* getters / setters */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDateTime dateDemande) { this.dateDemande = dateDemande; }

    public LocalDateTime getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDateTime datePaiement) { this.datePaiement = datePaiement; }

    public String getPaymeeReference() { return paymeeReference; }
    public void setPaymeeReference(String paymeeReference) { this.paymeeReference = paymeeReference; }
}
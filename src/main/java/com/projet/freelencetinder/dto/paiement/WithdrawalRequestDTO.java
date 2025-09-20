package com.projet.freelencetinder.dto.paiement;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class WithdrawalRequestDTO {

    @NotNull
    private Long methodId;

    @NotNull @DecimalMin("0.0")
    private BigDecimal montant;

    private String devise = "TND"; // TND/EUR/USD

    /* getters / setters */
    public Long getMethodId() { return methodId; }
    public void setMethodId(Long methodId) { this.methodId = methodId; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }
}
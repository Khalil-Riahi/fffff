package com.projet.freelencetinder.dto;

public class ValidationLivraisonDTO {

    private Long transactionId;
    private boolean validationClient;   // true = OK, false = litige/refus

    /* ---------- Constructeurs ---------- */
    public ValidationLivraisonDTO() {}

    public ValidationLivraisonDTO(Long transactionId, boolean validationClient) {
        this.transactionId   = transactionId;
        this.validationClient = validationClient;
    }

    /* ---------- Getters / Setters ---------- */
    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public boolean isValidationClient() { return validationClient; }
    public void setValidationClient(boolean validationClient) {
        this.validationClient = validationClient;
    }
}

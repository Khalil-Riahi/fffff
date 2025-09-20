package com.projet.freelencetinder.dto.paiement;

import jakarta.validation.constraints.NotBlank;

public class FlouciWebhookDTO {

    @NotBlank
    private String token;    // identifiant du paiement

    @NotBlank
    private String status;   // PAID | FAILED | CANCELLED

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

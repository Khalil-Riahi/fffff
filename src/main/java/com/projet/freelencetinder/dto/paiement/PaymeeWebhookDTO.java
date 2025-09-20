package com.projet.freelencetinder.dto.paiement;

import jakarta.validation.constraints.NotBlank;

public class PaymeeWebhookDTO {

    @NotBlank
    private String token;    // ✅ nom réel dans les webhooks v2

    @NotBlank
    private String status;

    public String getToken()                 { return token; }
    public void   setToken(String token)     { this.token = token; }

    public String getStatus()                { return status; }
    public void   setStatus(String status)   { this.status = status; }
}
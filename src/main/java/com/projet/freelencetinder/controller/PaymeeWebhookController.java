package com.projet.freelencetinder.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.projet.freelencetinder.dto.paiement.PaymeeWebhookDTO;
import com.projet.freelencetinder.servcie.EscrowService;

@RestController
@RequestMapping("/api/v1/paymee")
public class PaymeeWebhookController {

    private final EscrowService escrow;

    public PaymeeWebhookController(EscrowService escrow) { this.escrow = escrow; }

    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.OK)
    public void handle(@RequestBody PaymeeWebhookDTO dto) {
        escrow.handleWebhookPaiement(dto.getToken(), dto.getStatus());
    }
}
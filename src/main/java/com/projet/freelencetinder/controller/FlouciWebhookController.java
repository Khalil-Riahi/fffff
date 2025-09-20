// FlouciWebhookController.java
package com.projet.freelencetinder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.projet.freelencetinder.client.flouci.FlouciClient;
import com.projet.freelencetinder.dto.paiement.FlouciWebhookDTO;
import com.projet.freelencetinder.servcie.EscrowService;

@RestController
@RequestMapping("/api/v1/flouci")
public class FlouciWebhookController {

    private static final Logger log = LoggerFactory.getLogger(FlouciWebhookController.class);

    private final EscrowService escrowService;
    private final FlouciClient flouciClient;
    private final ObjectMapper om = new ObjectMapper();

    public FlouciWebhookController(EscrowService escrowService, FlouciClient flouciClient) {
        this.escrowService = escrowService;
        this.flouciClient = flouciClient;
    }

    /** Webhook Flouci (mode DIRECT_FLOUCI) */
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestHeader(value = "X-Flouci-Signature", required = false) String signature,
            @RequestBody String rawBody) {

        try {
            // (optionnel) vérif signature – en mode mock on renvoie true
            boolean ok = flouciClient.verifySignature(signature, rawBody == null ? "" : rawBody);
            if (!ok) {
                log.warn("Signature Flouci invalide");
                return ResponseEntity.badRequest().body("bad signature");
            }

            FlouciWebhookDTO payload = om.readValue(rawBody, FlouciWebhookDTO.class);
            escrowService.handleWebhookFlouci(payload.getToken(), payload.getStatus());
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Erreur webhook Flouci", e);
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
}

package com.projet.freelencetinder.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.projet.freelencetinder.servcie.EscrowService;

/**
 * Pages & endpoints pour SIMULER un paiement Flouci en local.
 * Le token est celui retourné par initPaiementDirect() via FlouciClient (mode mock).
 */
@RestController
@RequestMapping("/api/v1/flouci/_simulate")
public class FlouciSimulationController {

    private final EscrowService escrowService;

    public FlouciSimulationController(EscrowService escrowService) {
        this.escrowService = escrowService;
    }

    /** Petite page HTML avec deux boutons : Payer / Annuler */
    @GetMapping(value = "/pay/{token}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> fakePayPage(@PathVariable String token,
                                              @RequestParam(required = false) String amount,
                                              @RequestParam(required = false) String currency,
                                              @RequestParam(required = false, name = "ref") String reference) {

        String html = """
            <html><head><meta charset="utf-8"><title>Simulation Flouci</title></head>
            <body style="font-family: system-ui; padding: 24px;">
              <h2>Simulation de paiement Flouci</h2>
              <p><b>Token:</b> %s</p>
              <p><b>Montant:</b> %s %s</p>
              <p><b>Référence:</b> %s</p>
              <hr/>
              <form method="POST" action="/api/v1/flouci/_simulate/confirm/%s?status=PAID">
                <button style="padding:10px 16px;">✅ Simuler PAIEMENT (PAID)</button>
              </form>
              <br/>
              <form method="POST" action="/api/v1/flouci/_simulate/confirm/%s?status=CANCELLED">
                <button style="padding:10px 16px;">❌ Simuler ANNULATION</button>
              </form>
            </body></html>
            """.formatted(token,
                          amount == null ? "-" : amount,
                          currency == null ? "" : currency,
                          reference == null ? "-" : reference,
                          token, token);

        return ResponseEntity.ok(html);
    }

    /** Déclenche le même flux que le webhook Flouci réel. */
    @RequestMapping(value = "/confirm/{token}", method = { RequestMethod.POST, RequestMethod.GET })
    public ResponseEntity<String> confirm(@PathVariable String token,
                                          @RequestParam(defaultValue = "PAID") String status) {
        String norm = status == null ? "PAID" : status.trim().toUpperCase();
        escrowService.handleWebhookFlouci(token, norm);
        return ResponseEntity.ok("Webhook simulé avec status=" + norm + " pour token=" + token);
    }
}

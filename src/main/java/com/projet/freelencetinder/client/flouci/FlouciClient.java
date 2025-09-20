package com.projet.freelencetinder.client.flouci;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.projet.freelencetinder.config.flouci.FlouciProperties;

import reactor.core.publisher.Mono;

@Component
public class FlouciClient {

    private final WebClient web;
    private final FlouciProperties props;
    private static final JsonMapper mapper = JsonMapper.builder().build();

    public FlouciClient(FlouciProperties props, WebClient.Builder builder) {
        this.props = props;

        // ✅ En mock: ne PAS définir de baseUrl/headers (évite "Bad authority")
        if (props.isMock()) {
            this.web = builder.build();
        } else {
            this.web = builder
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey())
                .build();
        }
    }

    public record PaymentLink(String token, String url) {}

    /**
     * Crée un lien de paiement Flouci.
     * - MOCK: pas d’appel HTTP, on génère un token & une URL locale.
     * - Réel : appel API Flouci.
     */
    public PaymentLink createPayment(BigDecimal amount, String currency,
                                     String reference, String payeeWallet) {

        // ---- MODE SIMULATION LOCALE ----
        if (props.isMock() || props.getApiKey() == null || props.getApiKey().startsWith("REPLACE_")) {
            String token = UUID.randomUUID().toString().replace("-", "");
            String url = "http://localhost:8080/api/v1/flouci/_simulate/pay/" + token
                       + "?amount=" + amount + "&currency=" + currency + "&ref=" + reference;
            return new PaymentLink(token, url);
        }

        // ---- MODE REEL ----
        ObjectNode body = mapper.createObjectNode()
            .put("amount",      amount)               // selon API: number/string
            .put("currency",    currency)
            .put("note",        reference)
            .put("redirectUrl", props.getReturnUrl())
            .put("cancelUrl",   props.getCancelUrl())
            .put("webhookUrl",  props.getWebhookUrl())
            .put("payeeWallet", payeeWallet);

        ObjectNode resp = web.post()
            .uri("/payments/create")
            .bodyValue(body)
            .retrieve()
            .onStatus(HttpStatusCode::isError,
                r -> r.bodyToMono(String.class)
                      .flatMap(msg -> Mono.error(new RuntimeException("Flouci API error: " + msg))))
            .bodyToMono(ObjectNode.class)
            .block();

        String token = resp.path("data").path("token").asText(null);
        String url   = resp.path("data").path("payment_url").asText(null);
        if (token == null || url == null) throw new RuntimeException("Réponse Flouci invalide (token/url manquants)");

        return new PaymentLink(token, url);
    }

    /** Vérifie la signature du webhook Flouci (mock = toujours OK). */
    public boolean verifySignature(String signature, String rawBody) {
        if (props.isMock()) return true;
        // TODO: HMAC/secret selon doc Flouci si compte marchand réel
        return true;
    }
}

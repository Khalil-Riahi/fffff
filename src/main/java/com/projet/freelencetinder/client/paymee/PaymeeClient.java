package com.projet.freelencetinder.client.paymee;

import java.math.BigDecimal;

import reactor.core.publisher.Mono;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.projet.freelencetinder.config.paymee.PaymeeProperties;
import com.projet.freelencetinder.exception.PaymeeApiException;

@Component
public class PaymeeClient {

    private final WebClient        web;
    private final PaymeeProperties props;
    private static final JsonMapper mapper = JsonMapper.builder().build();

    public PaymeeClient(PaymeeProperties props, WebClient.Builder builder) {
        this.props = props;
        /*  ⚠️ base-url = https://sandbox.paymee.tn/api (sans /v1)  */
        this.web = builder
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /** Création d’un checkout Paymee (v2) – retourne {token, paymentUrl}. */
    public PaymeeCheckout createCheckout(BigDecimal amount, String currency, String reference) {

        ObjectNode body = mapper.createObjectNode()
                .put("vendor",        props.getVendor())          // id commerçant
                .put("amount",        amount)
                .put("note",          reference)
                .put("currency",      currency)
                .put("redirect_url",  props.getReturnUrl())
                .put("cancel_url",    props.getCancelUrl())
                .put("webhook_url",   props.getWebhookUrl())
                .put("first_name",    props.getFirstName())
                .put("last_name",     props.getLastName())
                .put("email",         props.getEmail())
                .put("phone",         props.getPhoneNumber());   // ✅ champ correct

        ObjectNode response = web.post()
                .uri("/v2/payments/create")
                .header("Authorization", "Token " + props.getPrivateKey())
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        r -> r.bodyToMono(String.class)
                              .flatMap(msg -> Mono.error(new PaymeeApiException(msg))))
                .bodyToMono(ObjectNode.class)
                .doOnNext(r -> System.out.println("Réponse Paymee : " + r.toPrettyString()))
                .block();

        /* -------- parsing de la réponse -------- */
        String token       = response.path("data").path("token").asText(null);
        String paymentUrl  = response.path("data").path("payment_url").asText(null);

        if (token == null || paymentUrl == null) {
            throw new PaymeeApiException("Champs token / payment_url manquants dans la réponse Paymee");
        }
        return new PaymeeCheckout(token, paymentUrl);
    }

    /** Capture / versement (si nécessaire). */
    public void transferToFreelance(String token) {
        web.post()
           .uri("/v2/payments/" + token + "/capture")
           .header("Authorization", "Token " + props.getPrivateKey())
           .retrieve()
           .onStatus(HttpStatusCode::isError,
                     r -> r.bodyToMono(String.class)
                           .flatMap(msg -> Mono.error(new PaymeeApiException(msg))))
           .toBodilessEntity()
           .block();
    }

    /** Payout (v2) vers un bénéficiaire (RIB ou wallet). */
    public String payout(BigDecimal amount, String currency, String beneficiary, String reference) {
        ObjectNode body = mapper.createObjectNode()
                .put("vendor",        props.getVendor())
                .put("amount",        amount)
                .put("currency",      currency)
                .put("note",          reference)
                .put("beneficiary",   beneficiary);

        ObjectNode response = web.post()
           .uri("/v2/payouts/create")
           .header("Authorization", "Token " + props.getPrivateKey())
           .bodyValue(body)
           .retrieve()
           .onStatus(HttpStatusCode::isError,
                     r -> r.bodyToMono(String.class)
                           .flatMap(msg -> Mono.error(new PaymeeApiException(msg))))
           .bodyToMono(ObjectNode.class)
           .block();

        String payoutId = response.path("data").path("payout_id").asText(null);
        if (payoutId == null) throw new PaymeeApiException("Référence payout manquante");
        return payoutId;
    }

    public record PaymeeCheckout(String id, String paymentUrl) {}
}
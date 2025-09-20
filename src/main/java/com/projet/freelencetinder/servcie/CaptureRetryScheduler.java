package com.projet.freelencetinder.servcie;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.projet.freelencetinder.models.TranchePaiement;
import com.projet.freelencetinder.models.TranchePaiement.StatutTranche;
import com.projet.freelencetinder.repository.TranchePaiementRepository;

/**
 * Tâches planifiées :
 * 1) Retry capture (ESCROW_PAYMEE uniquement) pour les tranches en erreur.
 * 2) Rappels / timeouts si aucune action après X jours (communs aux deux modes).
 */
@Component
public class CaptureRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(CaptureRetryScheduler.class);

    private final TranchePaiementRepository repo;
    private final EscrowService escrow;

    @Value("${payment.mode:DIRECT_FLOUCI}")
    private String paymentMode; // "DIRECT_FLOUCI" | "ESCROW_PAYMEE"

    private static final ZoneId TUNIS_TZ = ZoneId.of("Africa/Tunis");

    public CaptureRetryScheduler(TranchePaiementRepository repo, EscrowService escrow) {
        this.repo   = repo;
        this.escrow = escrow;
    }

    /** Re-essaye la capture toutes les 30 min (uniquement en ESCROW_PAYMEE). */
    @Scheduled(cron = "0 */30 * * * *", zone = "Africa/Tunis")
    public void retryFailedCaptures() {
        if (!isEscrowMode()) return; // rien à faire en mode direct (Flouci)
        List<TranchePaiement> list = repo.findByStatut(StatutTranche.ERREUR_CAPTURE);
        for (TranchePaiement t : list) {
            try {
                log.info("[ESCROW] Retry capture tranche {}", t.getId());
                escrow.retryCapture(t.getId());
            } catch (Exception ex) {
                log.error("[ESCROW] Retry capture échoué pour tranche {}", t.getId(), ex);
            }
        }
    }

    /** Vérifie les expirations et génère des logs (ou notifications) une fois par jour. */
    @Scheduled(cron = "0 0 3 * * *", zone = "Africa/Tunis")
    public void checkTimeouts() {
        LocalDateTime now = LocalDateTime.now(TUNIS_TZ);
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        // 1) Tranches en attente de paiement (> 7j) — commun aux deux modes
        repo.findByStatut(StatutTranche.EN_ATTENTE_PAIEMENT).stream()
            .filter(t -> t.getDateDepot() != null && t.getDateDepot().isBefore(sevenDaysAgo))
            .forEach(t -> log.warn("Tranche {} en attente de paiement depuis > 7 jours", t.getId()));

        if (isEscrowMode()) {
            // 2) Fonds bloqués sans validation client (> 7j) — spécifique escrow
            repo.findByStatut(StatutTranche.FONDS_BLOQUES).stream()
                .filter(t -> t.getDateDepot() != null && t.getDateDepot().isBefore(sevenDaysAgo))
                .forEach(t -> log.warn("[ESCROW] Tranche {} fonds bloqués sans validation (>7j)", t.getId()));

            // 3) VALIDEE mais pas encore versée (> 2j) — cas d’erreur silencieuse
            LocalDateTime twoDaysAgo = now.minusDays(2);
            repo.findByStatut(StatutTranche.VALIDEE).stream()
                .filter(t -> t.getDateValidation() != null && t.getDateValidation().isBefore(twoDaysAgo))
                .forEach(t -> log.warn("[ESCROW] Tranche {} validée depuis >2j sans versement", t.getId()));
        }
    }

    private boolean isEscrowMode() {
        return "ESCROW_PAYMEE".equalsIgnoreCase(paymentMode);
    }
}

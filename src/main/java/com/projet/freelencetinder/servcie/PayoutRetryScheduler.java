package com.projet.freelencetinder.servcie;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.projet.freelencetinder.models.WithdrawalRequest;
import com.projet.freelencetinder.models.WithdrawalRequest.Statut;
import com.projet.freelencetinder.repository.WithdrawalRequestRepository;

@Component
public class PayoutRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(PayoutRetryScheduler.class);

    private final PayoutService payoutService;
    private final WithdrawalRequestRepository requestRepo;

    public PayoutRetryScheduler(PayoutService payoutService,
                                WithdrawalRequestRepository requestRepo) {
        this.payoutService = payoutService;
        this.requestRepo = requestRepo;
    }

    /**
     * Relance les payouts en erreur toutes les 10 minutes.
     */
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void retryFailedPayouts() {
        List<WithdrawalRequest> list = requestRepo.findAll()
                .stream()
                .filter(r -> r.getStatut() == Statut.ERREUR)
                .toList();
        for (WithdrawalRequest r : list) {
            try {
                payoutService.retryPayout(r.getId());
            } catch (Exception ex) {
                log.error("Retry payout échoué pour {}", r.getId(), ex);
            }
        }
    }
}
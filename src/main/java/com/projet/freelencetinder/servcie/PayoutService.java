package com.projet.freelencetinder.servcie;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projet.freelencetinder.client.paymee.PaymeeClient;
import com.projet.freelencetinder.dto.paiement.*;
import com.projet.freelencetinder.exception.BusinessException;
import com.projet.freelencetinder.models.*;
import com.projet.freelencetinder.models.WithdrawalMethod.Type;
import com.projet.freelencetinder.models.WithdrawalRequest.Statut;
import com.projet.freelencetinder.repository.*;

@Service
public class PayoutService {

    private static final Logger log = LoggerFactory.getLogger(PayoutService.class);

    private final UtilisateurRepository userRepo;
    private final WithdrawalMethodRepository methodRepo;
    private final WithdrawalRequestRepository requestRepo;
    private final PaymeeClient paymee;
    private final PaymentAuditRepository auditRepo;

    public PayoutService(UtilisateurRepository userRepo,
                         WithdrawalMethodRepository methodRepo,
                         WithdrawalRequestRepository requestRepo,
                         PaymeeClient paymee,
                         PaymentAuditRepository auditRepo) {
        this.userRepo = userRepo;
        this.methodRepo = methodRepo;
        this.requestRepo = requestRepo;
        this.paymee = paymee;
        this.auditRepo = auditRepo;
    }

    /* ---------- Méthodes de retrait ---------- */
    @Transactional
    public WithdrawalMethodResponseDTO addMethod(Long freelanceId, WithdrawalMethodCreateDTO dto) {
        Utilisateur freelance = userRepo.findById(freelanceId)
                .orElseThrow(() -> new BusinessException("Freelance introuvable"));

        // Récupération verrouillée pour éviter les conditions de course
        List<WithdrawalMethod> existing = methodRepo.findByFreelanceIdForUpdate(freelanceId);

        WithdrawalMethod m = new WithdrawalMethod();
        m.setFreelance(freelance);
        m.setType(Type.valueOf(dto.getType().toUpperCase()));

        switch (m.getType()) {
            case RIB -> {
                if (dto.getRib() == null || !dto.getRib().matches("^TN59\\d{20}$"))
                    throw new BusinessException("RIB (IBAN) tunisien invalide");
                m.setRib(dto.getRib());
            }
            case D17 -> {
                if (dto.getWalletNumber() == null || !dto.getWalletNumber().matches("^[0-9]{8}$"))
                    throw new BusinessException("Numéro D17 invalide (8 chiffres)");
                m.setWalletNumber(dto.getWalletNumber());
            }
        }

        boolean setAsPrimary = dto.isPrincipal() || existing.stream().noneMatch(WithdrawalMethod::isPrincipal);
        m.setPrincipal(setAsPrimary);
        existing.add(m);

        if (setAsPrimary) {
            existing.stream()
                    .filter(w -> w != m && w.isPrincipal())
                    .forEach(w -> w.setPrincipal(false));
        }

        methodRepo.saveAll(existing);

        logEventGeneric(freelanceId, "METHODE_RETRAIT_AJOUTEE", "Id=" + m.getId());

        return toDto(m);
    }

    @Transactional(readOnly = true)
    public List<WithdrawalMethodResponseDTO> listMethods(Long freelanceId) {
        return methodRepo.findByFreelanceId(freelanceId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /* ---------- Demande de retrait ---------- */
    @Transactional
    public WithdrawalRequestResponseDTO requestWithdrawal(Long freelanceId, WithdrawalRequestDTO dto) {
        Utilisateur freelance = userRepo.findById(freelanceId)
                .orElseThrow(() -> new BusinessException("Freelance introuvable"));

        WithdrawalMethod method = methodRepo.findById(dto.getMethodId())
                .orElseThrow(() -> new BusinessException("Méthode de retrait inconnue"));

        if (!method.getFreelance().getId().equals(freelanceId))
            throw new BusinessException("Non autorisé");

        BigDecimal solde = freelance.getSoldeEscrow() == null ? BigDecimal.ZERO : freelance.getSoldeEscrow();
        if (solde.compareTo(dto.getMontant()) < 0)
            throw new BusinessException("Solde insuffisant");

        // Déduction immédiate
        freelance.setSoldeEscrow(solde.subtract(dto.getMontant()));
        userRepo.save(freelance);

        WithdrawalRequest req = new WithdrawalRequest();
        req.setFreelance(freelance);
        req.setMethod(method);
        req.setMontant(dto.getMontant());
        req.setDevise(Devise.from(dto.getDevise()));

        requestRepo.save(req);

        logEventForWithdrawal(req.getId(), "RETRAIT_DEMANDE", "Montant=" + dto.getMontant());

        // Appel Paymee
        try {
            String beneficiary = method.getType() == Type.RIB ? method.getRib() : method.getWalletNumber();
            String paymeeRef = paymee.payout(dto.getMontant(), dto.getDevise(), beneficiary, "Payout#" + req.getId());
            req.setStatut(Statut.PAYE);
            req.setDatePaiement(LocalDateTime.now());
            req.setPaymeeReference(paymeeRef);
            requestRepo.save(req);
            logEventForWithdrawal(req.getId(), "RETRAIT_PAYE", "Id=" + req.getId());
        } catch (Exception ex) {
            log.error("Erreur payout Paymee", ex);
            req.setStatut(Statut.ERREUR);
            requestRepo.save(req);
            freelance.setSoldeEscrow(freelance.getSoldeEscrow().add(dto.getMontant())); // rollback solde
            userRepo.save(freelance);
            logEventForWithdrawal(req.getId(), "RETRAIT_ERREUR", ex.getMessage());
        }

        return toDto(req);
    }

    @Transactional(readOnly = true)
    public List<WithdrawalRequestResponseDTO> listWithdrawals(Long freelanceId) {
        return requestRepo.findByFreelanceIdOrderByDateDemandeDesc(freelanceId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /* ---------- Retry manuel ---------- */
    @Transactional
    public void retryPayout(Long requestId) {
        WithdrawalRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new BusinessException("Withdrawal introuvable"));
        if (req.getStatut() != Statut.ERREUR) return;

        WithdrawalMethod method = req.getMethod();

        try {
            String beneficiary = method.getType() == Type.RIB ? method.getRib() : method.getWalletNumber();
            String ref = paymee.payout(req.getMontant(), req.getDevise().toString(), beneficiary, "RetryPayout#" + req.getId());
            req.setStatut(Statut.PAYE);
            req.setDatePaiement(java.time.LocalDateTime.now());
            req.setPaymeeReference(ref);
            requestRepo.save(req);
            logEventForWithdrawal(req.getId(), "RETRAIT_PAYE_RETRY", "Id=" + req.getId());
        } catch (Exception ex) {
            log.error("Retry payout Paymee échoué", ex);
        }
    }

    /* ---------- Helpers ---------- */
    private WithdrawalMethodResponseDTO toDto(WithdrawalMethod m) {
        WithdrawalMethodResponseDTO dto = new WithdrawalMethodResponseDTO();
        dto.setId(m.getId());
        dto.setType(m.getType().name());
        dto.setRib(m.getRib());
        dto.setWalletNumber(m.getWalletNumber());
        dto.setPrincipal(m.isPrincipal());
        dto.setDateAjout(m.getDateAjout());
        return dto;
    }

    private WithdrawalRequestResponseDTO toDto(WithdrawalRequest r) {
        WithdrawalRequestResponseDTO dto = new WithdrawalRequestResponseDTO();
        dto.setId(r.getId());
        dto.setMontant(r.getMontant());
        dto.setDevise(r.getDevise().toString());
        dto.setStatut(r.getStatut().name());
        dto.setDateDemande(r.getDateDemande());
        dto.setDatePaiement(r.getDatePaiement());
        dto.setPaymeeReference(r.getPaymeeReference());
        return dto;
    }

    private void logEventGeneric(Long userId, String event, String details) {
        PaymentAudit a = new PaymentAudit();
        a.setTrancheId(userId); // usage générique (comme dans ta version)
        a.setEvent(event);
        a.setDetails(details);
        auditRepo.save(a);
    }

    private void logEventForWithdrawal(Long withdrawalRequestId, String event, String details) {
        PaymentAudit a = new PaymentAudit();
        a.setWithdrawalRequestId(withdrawalRequestId);
        a.setEvent(event);
        a.setDetails(details);
        auditRepo.save(a);
    }
}

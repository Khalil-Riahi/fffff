/* PayoutController.java */
package com.projet.freelencetinder.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.projet.freelencetinder.dto.paiement.*;
import com.projet.freelencetinder.repository.UtilisateurRepository;
import com.projet.freelencetinder.servcie.PayoutService;
import com.projet.freelencetinder.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/v1/payout")
public class PayoutController {

    private final PayoutService payout;
    private final UtilisateurRepository userRepo;

    public PayoutController(PayoutService payout,
                            UtilisateurRepository userRepo) {
        this.payout = payout;
        this.userRepo = userRepo;
    }

    /* --- Méthodes de retrait --- */
    @PreAuthorize("hasRole('FREELANCE')")
    @PostMapping("/methods")
    public ResponseEntity<WithdrawalMethodResponseDTO> addMethod(
            @Valid @RequestBody WithdrawalMethodCreateDTO dto) {
        return ResponseEntity.ok(
                payout.addMethod(getCurrentUserId(), dto));
    }

    @PreAuthorize("hasRole('FREELANCE')")
    @GetMapping("/methods")
    public List<WithdrawalMethodResponseDTO> listMethods() {
        return payout.listMethods(getCurrentUserId());
    }

    /* --- Demande de retrait --- */
    @PreAuthorize("hasRole('FREELANCE')")
    @PostMapping("/withdrawals")
    public ResponseEntity<WithdrawalRequestResponseDTO> requestWithdrawal(
            @Valid @RequestBody WithdrawalRequestDTO dto) {
        return ResponseEntity.ok(
                payout.requestWithdrawal(getCurrentUserId(), dto));
    }

    @PreAuthorize("hasRole('FREELANCE')")
    @GetMapping("/withdrawals")
    public List<WithdrawalRequestResponseDTO> listWithdrawals() {
        return payout.listWithdrawals(getCurrentUserId());
    }

    /* ----------- helpers ----------- */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepo.findByEmail(email)
                .map(com.projet.freelencetinder.models.Utilisateur::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }
}
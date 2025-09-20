package com.projet.freelencetinder.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.projet.freelencetinder.dto.paiement.TranchePaiementResponseDTO;
import com.projet.freelencetinder.exception.ResourceNotFoundException;
import com.projet.freelencetinder.repository.UtilisateurRepository;
import com.projet.freelencetinder.servcie.EscrowService;

/**
 * Endpoints “simples” pour initier un paiement direct (Flouci) côté client.
 * On déclenche le lien de paiement après que le livrable a été validé côté workflow.
 */
@RestController
@RequestMapping("/api/v1/tranches")
public class PaymentController {

    private final EscrowService escrowService;
    private final UtilisateurRepository userRepo;

    public PaymentController(EscrowService escrowService,
                             UtilisateurRepository userRepo) {
        this.escrowService = escrowService;
        this.userRepo = userRepo;
    }

    /**
     * Génère le lien de paiement direct (Flouci) pour la tranche donnée.
     * Le client consommera l’URL retournée pour payer.
     * On accepte X-User-Id (tests) mais on retombe sur l’utilisateur courant s’il est absent.
     */
    @PostMapping("/{trancheId}/payer-direct")
    public ResponseEntity<TranchePaiementResponseDTO> initierPaiementDirect(
            @PathVariable Long trancheId,
            @RequestHeader(value = "X-User-Id", required = false) Long clientId) {

        Long effectiveClientId = clientId != null ? clientId : getCurrentUserId();
        TranchePaiementResponseDTO dto = escrowService.initPaiementDirect(trancheId, effectiveClientId);
        return ResponseEntity.ok(dto);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepo.findByEmail(email)
                .map(com.projet.freelencetinder.models.Utilisateur::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }
}

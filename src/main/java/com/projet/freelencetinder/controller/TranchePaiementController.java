/* TranchePaiementController.java */
package com.projet.freelencetinder.controller;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.projet.freelencetinder.dto.paiement.*;
import com.projet.freelencetinder.servcie.EscrowService;
import com.projet.freelencetinder.repository.UtilisateurRepository;
import com.projet.freelencetinder.exception.ResourceNotFoundException;
import com.projet.freelencetinder.exception.BusinessException;

@RestController
@RequestMapping("/api/v1/paiement")
public class TranchePaiementController {

    private final EscrowService escrow;
    private final UtilisateurRepository userRepo;

    public TranchePaiementController(EscrowService escrow,
                                     UtilisateurRepository userRepo) {
        this.escrow = escrow;
        this.userRepo = userRepo;
    }

    /* ------ création d’une tranche ------ */
    @PostMapping("/tranches")
    public ResponseEntity<TranchePaiementResponseDTO> create(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @Valid @RequestBody TranchePaiementCreateDTO dto) {

        Long clientId = resolveUserId(headerUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(escrow.createTranche(dto, clientId));
    }

    /* ------ génération lien ESCROW (Paymee) ------ */
    @PostMapping("/tranches/{id}/checkout")
    public TranchePaiementResponseDTO checkout(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @PathVariable Long id) {

        return escrow.initPaiement(id, resolveUserId(headerUserId));
    }

    /* ------ validation livrable (escrow) ------ */
    @PostMapping("/tranches/{id}/valider")
    public TranchePaiementResponseDTO valider(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @PathVariable Long id) {

        return escrow.validerLivrable(id, resolveUserId(headerUserId));
    }

    /* ------ récap mission ------ */
    @GetMapping("/missions/{id}/summary")
    public MissionPaiementSummaryDTO summary(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @PathVariable Long id) {

        return escrow.summary(id, resolveUserId(headerUserId));
    }

    /* ------ marquer tranche finale / required ------ */
    @PatchMapping("/tranches/{id}/finale")
    public ResponseEntity<TranchePaiementResponseDTO> setFinale(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @PathVariable Long id,
            @RequestParam boolean value) {

        Long clientId = resolveUserId(headerUserId);
        return ResponseEntity.ok(escrow.markTrancheFinale(id, clientId, value));
    }

    @PatchMapping("/tranches/{id}/required")
    public ResponseEntity<TranchePaiementResponseDTO> setRequired(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @PathVariable Long id,
            @RequestParam boolean value) {

        Long clientId = resolveUserId(headerUserId);
        return ResponseEntity.ok(escrow.markTrancheRequired(id, clientId, value));
    }

    /* ----------- helpers ----------- */
    private Long resolveUserId(Long headerUserId) {
        // 1) MVP local : si X-User-Id présent, on l’utilise
        if (headerUserId != null) return headerUserId;

        // 2) Sinon, on tente l’authentification classique
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || "anonymousUser".equalsIgnoreCase(auth.getName())) {
            // Message clair pour les tests Postman
            throw new BusinessException("Non authentifié. En local, passe X-User-Id dans les headers (ex: 78).");
        }

        String email = auth.getName();
        return userRepo.findByEmail(email)
                .map(com.projet.freelencetinder.models.Utilisateur::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    /* ---- Handler temporaire pour tracer toutes les exceptions ---- */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.badRequest().body("Erreur : " + ex.getMessage());
    }
}

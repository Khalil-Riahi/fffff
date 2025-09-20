package com.projet.freelencetinder.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;

import com.projet.freelencetinder.dto.MissionRecommendationDTO;
import com.projet.freelencetinder.dto.MissionSummaryDTO;
import com.projet.freelencetinder.dto.FreelanceSummaryDTO;

import com.projet.freelencetinder.models.ClientSwipe;
import com.projet.freelencetinder.models.Mission.Categorie;
import com.projet.freelencetinder.models.Swipe;
import com.projet.freelencetinder.models.Swipe.Decision;
import com.projet.freelencetinder.servcie.SwipeService;

/**
 * Contrôleur REST dédié au système de swipe / matching / recommandations.
 * Toutes les opérations dynamiques liées aux interactions utilisateurs.
 */
@RestController
@RequestMapping("/api/swipes")
public class SwipeController {

    private final SwipeService swipeService;

    @Autowired
    public SwipeController(SwipeService swipeService) {
        this.swipeService = swipeService;
    }

    /* ================================================================
       1. Missions disponibles pour un freelance (carte swipe)
       GET /api/swipes/available?freelanceId=1&categorie=DEVELOPPEMENT_WEB
       ================================================================ */
    @GetMapping("/available")
    public ResponseEntity<List<MissionSummaryDTO>> getMissionsForSwipe(
            @RequestParam Long freelanceId,
            @RequestParam(required = false) Categorie categorie) {

        if (freelanceId == null)
            throw new IllegalArgumentException("freelanceId obligatoire");

        return ResponseEntity.ok(
                swipeService.getMissionsForSwipe(freelanceId, categorie)
        );
    }

    /* ================================================================
       2. Swipe FREELANCE → mission
       POST /api/swipes/mission/{missionId}/freelance/{freelanceId}?decision=LIKE[&dwellTimeMs=1234]
       OU body: { "decision": "LIKE", "dwellTimeMs": 1234 }
       ================================================================ */
    @PostMapping("/mission/{missionId}/freelance/{freelanceId}")
    public ResponseEntity<Swipe> swipeMission(@PathVariable Long missionId,
                                              @PathVariable Long freelanceId,
                                              @RequestParam(required = false) Decision decision,
                                              @RequestParam(required = false) Long dwellTimeMs, // AJOUT
                                              @RequestBody(required = false) SwipeRequest body) {

        Decision effective = resolveDecision(decision, body);
        Long     dwell     = resolveDwellTime(dwellTimeMs, body); // AJOUT
        Swipe swipe = swipeService.swipeMission(freelanceId, missionId, effective, dwell);
        return ResponseEntity.ok(swipe);
    }

    /* ================================================================
       3. Swipe CLIENT → freelance
       POST /api/swipes/mission/{missionId}/client/{clientId}/freelance/{freelanceId}?decision=LIKE[&dwellTimeMs=1234]
       OU body: { "decision": "LIKE", "dwellTimeMs": 1234 }
       ================================================================ */
    @PostMapping("/mission/{missionId}/client/{clientId}/freelance/{freelanceId}")
    public ResponseEntity<ClientSwipe> clientSwipeFreelance(
            @PathVariable Long missionId,
            @PathVariable Long clientId,
            @PathVariable Long freelanceId,
            @RequestParam(required = false) Decision decision,
            @RequestParam(required = false) Long dwellTimeMs, // AJOUT
            @RequestBody(required = false) SwipeRequest body) {

        Decision effective = resolveDecision(decision, body);
        Long     dwell     = resolveDwellTime(dwellTimeMs, body); // AJOUT
        ClientSwipe cs = swipeService.clientSwipeFreelance(
                clientId, missionId, freelanceId, effective, dwell);
        return ResponseEntity.ok(cs);
    }

    /* ================================================================
       4. Annuler swipe FREELANCE
       DELETE /api/swipes/mission/{missionId}/freelance/{freelanceId}
       ================================================================ */
    @DeleteMapping("/mission/{missionId}/freelance/{freelanceId}")
    public ResponseEntity<Void> removeSwipeFreelance(@PathVariable Long missionId,
                                                     @PathVariable Long freelanceId) {
        swipeService.removeSwipeFreelance(freelanceId, missionId);
        return ResponseEntity.noContent().build();
    }

    /* ================================================================
       5. Annuler swipe CLIENT
       DELETE /api/swipes/mission/{missionId}/client/{clientId}/freelance/{freelanceId}
       ================================================================ */
    @DeleteMapping("/mission/{missionId}/client/{clientId}/freelance/{freelanceId}")
    public ResponseEntity<Void> removeSwipeClient(@PathVariable Long missionId,
                                                  @PathVariable Long clientId,
                                                  @PathVariable Long freelanceId) {
        swipeService.removeSwipeClient(clientId, missionId, freelanceId);
        return ResponseEntity.noContent().build();
    }

    /* ================================================================
       6. Affectation manuelle (override)
       POST /api/swipes/mission/{missionId}/assign?freelanceId=5
       ================================================================ */
    @PostMapping("/mission/{missionId}/assign")
    public ResponseEntity<Void> assignFreelance(@PathVariable Long missionId,
                                                @RequestParam Long freelanceId) {
        swipeService.assignFreelanceToMission(missionId, freelanceId);
        return ResponseEntity.ok().build();
    }

    /* ================================================================
       7. Recommandations pour un freelance
       GET /api/swipes/recommandations?freelanceId=1
       ================================================================ */
    @GetMapping("/recommandations")
    public ResponseEntity<List<MissionRecommendationDTO>> getRecommandations(
            @RequestParam Long freelanceId) {
        if (freelanceId == null)
            throw new IllegalArgumentException("freelanceId obligatoire");
        return ResponseEntity.ok(swipeService.getRecommandationsPourFreelance(freelanceId));
    }

    /* ================================================================
       8. Voir les freelances ayant liké une mission (vue client)
       GET /api/swipes/mission/{missionId}/likes?clientId=123
       ================================================================ */
    @GetMapping("/mission/{missionId}/likes")
    public ResponseEntity<List<FreelanceSummaryDTO>> getFreelancersWhoLikedMission(
            @PathVariable Long missionId,
            @RequestParam Long clientId) {

        if (clientId == null) {
            throw new IllegalArgumentException("clientId est requis");
        }

        List<FreelanceSummaryDTO> freelancers =
            swipeService.getFreelancersWhoLikedMission(clientId, missionId);

        return ResponseEntity.ok(freelancers);
    }

    /**
     * Explorer les freelances compatibles avec une mission (matching compétences).
     * GET /api/swipes/mission/{missionId}/explore?clientId=123
     */
    @GetMapping("/mission/{missionId}/explore")
    public ResponseEntity<List<FreelanceSummaryDTO>> exploreFreelancers(
            @PathVariable Long missionId,
            @RequestParam Long clientId) {

        if (clientId == null) {
            throw new IllegalArgumentException("clientId est requis");
        }
        List<FreelanceSummaryDTO> result =
            swipeService.getFreelancersMatchingMission(clientId, missionId);
        return ResponseEntity.ok(result);
    }

    /* ================================================================
       Gestion locale d’erreurs (optionnel)
       ================================================================ */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /* ================================================================
       Helpers internes
       ================================================================ */
    private Decision resolveDecision(Decision param, SwipeRequest body) {
        if (param != null) return param;
        if (body != null && body.getDecision() != null) return body.getDecision();
        throw new IllegalArgumentException("Decision (LIKE ou DISLIKE) requise");
    }

    // récupère dwellTimeMs soit via query param, soit via body
    private Long resolveDwellTime(Long param, SwipeRequest body) {
        if (param != null) return param;
        if (body != null) return body.getDwellTimeMs();
        return null;
    }

    /* ================================================================
       DTO interne pour requêtes (body JSON)
       ================================================================ */
    public static class SwipeRequest {
        private Decision decision;
        private Long dwellTimeMs; // analytics

        public Decision getDecision() { return decision; }
        public void setDecision(Decision decision) { this.decision = decision; }

        public Long getDwellTimeMs() { return dwellTimeMs; }
        public void setDwellTimeMs(Long dwellTimeMs) { this.dwellTimeMs = dwellTimeMs; }
    }
}

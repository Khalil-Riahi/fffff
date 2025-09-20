package com.projet.freelencetinder.controller;

import static org.springframework.http.HttpHeaders.USER_AGENT;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.projet.freelencetinder.dto.FeedbackDTOs.*;
import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.repository.MissionRepository;
import com.projet.freelencetinder.repository.UtilisateurRepository;
import com.projet.freelencetinder.servcie.FeedbackService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/feedback")
@Validated
public class FeedbackController extends BaseSecuredController {

    private final FeedbackService service;
    private final MissionRepository missionRepo;

    public FeedbackController(FeedbackService service,
                              MissionRepository missionRepo,
                              UtilisateurRepository userRepo) {
        super(userRepo);
        this.service = service;
        this.missionRepo = missionRepo;
    }

    @GetMapping("/eligibility")
    public ResponseEntity<FeedbackEligibilityDTO> eligibility(@RequestParam Long missionId) {
        Long uid = getCurrentUserId();
        Mission m = missionRepo.findById(missionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "MISSION_NOT_FOUND"));
        FeedbackEligibilityDTO dto = service.getEligibility(missionId, uid, m.getClient().getId(), m.getFreelanceSelectionne() != null ? m.getFreelanceSelectionne().getId() : null);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/window")
    public ResponseEntity<FeedbackWindowDTO> window(@RequestParam Long missionId) {
        return ResponseEntity.ok(service.getWindow(missionId));
    }

    @PostMapping("/submit")
    public ResponseEntity<FeedbackResponseDTO> submit(@Valid @RequestBody FeedbackCreateRequestDTO body,
                                                      HttpServletRequest request) {
        Long uid = getCurrentUserId();
        String ip = request.getRemoteAddr();
        String ua = request.getHeader(USER_AGENT);
        String ipHash = sha256(ip);
        String uaHash = sha256(ua != null ? ua : "");
        FeedbackResponseDTO dto = service.submit(body, uid, ipHash, uaHash);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/public")
    public ResponseEntity<Page<FeedbackResponseDTO>> list(FeedbackListQueryDTO q) {
        return ResponseEntity.ok(service.listPublic(q));
    }

    @GetMapping("/summary")
    public ResponseEntity<FeedbackSummaryDTO> summary(@RequestParam Long targetId,
                                                      @RequestParam com.projet.freelencetinder.models.Audience audience,
                                                      @RequestParam(defaultValue = "90") int windowDays) {
        return ResponseEntity.ok(service.summary(targetId, audience, windowDays));
    }

    @GetMapping("/{feedbackId}")
    public ResponseEntity<FeedbackResponseDTO> getFeedback(@PathVariable Long feedbackId) {
        Long uid = getCurrentUserId();
        FeedbackResponseDTO dto = service.getOwnFeedback(feedbackId, uid);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/by-mission/{missionId}")
    public ResponseEntity<FeedbackResponseDTO> getFeedbackByMission(@PathVariable Long missionId) {
        Long uid = getCurrentUserId();
        FeedbackResponseDTO dto = service.getOwnFeedbackByMission(missionId, uid);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{feedbackId}")
    public ResponseEntity<FeedbackResponseDTO> updateFeedback(@PathVariable Long feedbackId,
                                                              @Valid @RequestBody FeedbackUpdateRequestDTO body,
                                                              HttpServletRequest request) {
        Long uid = getCurrentUserId();
        String ip = request.getRemoteAddr();
        String ua = request.getHeader(USER_AGENT);
        String ipHash = sha256(ip);
        String uaHash = sha256(ua != null ? ua : "");
        FeedbackResponseDTO dto = service.updateFeedback(feedbackId, body, uid, ipHash, uaHash);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long feedbackId) {
        Long uid = getCurrentUserId();
        service.deleteFeedback(feedbackId, uid);
        return ResponseEntity.noContent().build();
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}



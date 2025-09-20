package com.projet.freelencetinder.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.projet.freelencetinder.dto.NotificationDTO;
import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.repository.UtilisateurRepository;
import com.projet.freelencetinder.servcie.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService        notificationService;
    private final UtilisateurRepository      userRepo;

    public NotificationController(NotificationService notificationService,
                                  UtilisateurRepository userRepo) {
        this.notificationService = notificationService;
        this.userRepo            = userRepo;
    }

    /* =======================================================
       1. Feed paginé (cloche)
       GET /api/notifications?size=20&page=0
       ======================================================= */
    @GetMapping
    public Page<NotificationDTO> getLatest(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = currentUserId();
        return notificationService.latest(userId, page, size);
    }

    /* =======================================================
       2. Marquer une notification comme vue
       POST /api/notifications/{id}/seen
       ======================================================= */
    @PostMapping("/{id}/seen")
    public ResponseEntity<Void> markSeen(@PathVariable Long id) {
        notificationService.markSeen(id, currentUserId());
        return ResponseEntity.ok().build();
    }

    /* =======================================================
       3. Marquer TOUTES les notifications comme vues
       POST /api/notifications/seen-all
       ======================================================= */
    @PostMapping("/seen-all")
    public ResponseEntity<Void> markAllSeen() {
        notificationService.markAllSeen(currentUserId());
        return ResponseEntity.ok().build();
    }

    /* =======================================================
       4. Archiver toutes les notifications déjà vues
       POST /api/notifications/archive-seen
       ======================================================= */
    @PostMapping("/archive-seen")
    public ResponseEntity<Void> archiveSeen() {
        notificationService.archiveSeen(currentUserId());
        return ResponseEntity.ok().build();
    }

    /* =======================================================
       Helper : récupère l’ID du user connecté
       ======================================================= */
    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("Utilisateur non authentifié");
        }
        String email = auth.getName();
        Utilisateur u = userRepo.findByEmail(email)
            .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable pour " + email));
        return u.getId();
    }
}

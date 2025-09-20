package com.projet.freelencetinder.controller;

import com.projet.freelencetinder.dto.ConversationSummaryDto;
import com.projet.freelencetinder.models.Conversation;
import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.repository.UtilisateurRepository;
import com.projet.freelencetinder.servcie.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController extends BaseSecuredController {

    private final ConversationService convService;
    private final UtilisateurRepository userRepo;

    public ConversationController(ConversationService convService,
                                  UtilisateurRepository userRepo) {
        super(userRepo);
        this.convService = convService;
        this.userRepo = userRepo;
    }

    /* ------------------------------------------------------- */
    @GetMapping
    public List<ConversationSummaryDto> listUserConversations(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        Long currentUserId = getCurrentUserId();
        return convService.getUserConversations(currentUserId, page, size);
    }

    /* -------------------------------------------------------
       Création / récupération conversation :
       On envoie missionId + otherUserId.
       Le système détermine qui est client/freelance.
       ------------------------------------------------------- */
    @PostMapping("/init")
    public ResponseEntity<Conversation> createOrGetConversation(@RequestParam Long missionId,
                                                                @RequestParam Long otherUserId) {

        Long currentUserId = getCurrentUserId();
        if (currentUserId.equals(otherUserId)) {
            throw new IllegalArgumentException("otherUserId invalide (identique à l’utilisateur courant)");
        }

        Utilisateur current = userRepo.getReferenceById(currentUserId);
        Utilisateur other = userRepo.getReferenceById(otherUserId);

        Long clientId;
        Long freelanceId;

        // Détermine les rôles
        if (current.getTypeUtilisateur() == Utilisateur.TypeUtilisateur.CLIENT
                && other.getTypeUtilisateur() == Utilisateur.TypeUtilisateur.FREELANCE) {
            clientId = currentUserId;
            freelanceId = otherUserId;
        } else if (current.getTypeUtilisateur() == Utilisateur.TypeUtilisateur.FREELANCE
                && other.getTypeUtilisateur() == Utilisateur.TypeUtilisateur.CLIENT) {
            clientId = otherUserId;
            freelanceId = currentUserId;
        } else {
            throw new IllegalStateException("Incohérence des rôles CLIENT/FREELANCE pour créer une conversation.");
        }

        Conversation conv = convService.findOrCreate(missionId, clientId, freelanceId);
        return ResponseEntity.ok(conv);
    }
}

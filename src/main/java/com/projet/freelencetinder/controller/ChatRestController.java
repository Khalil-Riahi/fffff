package com.projet.freelencetinder.controller;

import com.projet.freelencetinder.dto.ChatMessageResponse;
import com.projet.freelencetinder.repository.UtilisateurRepository;
import com.projet.freelencetinder.servcie.ChatMessageService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController extends BaseSecuredController {

    private final ChatMessageService chatService;

    public ChatRestController(ChatMessageService chatService,
                              UtilisateurRepository userRepo) {
        super(userRepo);
        this.chatService = chatService;
    }

    /* ------------------------------------------------------- */
    @GetMapping("/conversations/{id}/messages")
    public Page<ChatMessageResponse> getMessages(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {

        Long currentUserId = getCurrentUserId();
        return chatService.getConversationMessages(id, page, size, currentUserId);
    }

    /* ------------------------------------------------------- */
    @PutMapping("/conversations/{id}/seen")
    public ResponseEntity<Void> markSeen(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        chatService.markConversationSeen(id, currentUserId);
        return ResponseEntity.ok().build();
    }
}

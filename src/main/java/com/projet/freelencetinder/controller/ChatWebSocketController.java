package com.projet.freelencetinder.controller;

import com.projet.freelencetinder.dto.ChatMessageResponse;
import com.projet.freelencetinder.dto.SendMessageRequest;
import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.repository.UtilisateurRepository;
import com.projet.freelencetinder.servcie.ChatMessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final ChatMessageService chatService;
    private final UtilisateurRepository userRepo;

    public ChatWebSocketController(ChatMessageService chatService,
                                   UtilisateurRepository userRepo) {
        this.chatService = chatService;
        this.userRepo = userRepo;
    }

    @MessageMapping("/chat/send")          // front => /app/chat/send
    @SendToUser("/queue/ack")
    public ChatMessageResponse handleSend(@Payload SendMessageRequest req,
                                          StompHeaderAccessor accessor) {

        String principalName = accessor.getUser().getName(); // email ou id selon ta config
        Long senderId;

        try {
            // Si déjà un ID numérique
            senderId = Long.valueOf(principalName);
        } catch (NumberFormatException e) {
            // Sinon on suppose que c’est un email
            Utilisateur u = userRepo.findByEmail(principalName)
                    .orElseThrow(() -> new IllegalStateException("Utilisateur WS introuvable: " + principalName));
            senderId = u.getId();
        }

        return chatService.sendMessage(req, senderId);
    }
}

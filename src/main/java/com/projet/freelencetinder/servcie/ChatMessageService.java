package com.projet.freelencetinder.servcie;

import com.projet.freelencetinder.dto.ChatMessageResponse;
import com.projet.freelencetinder.dto.SendMessageRequest;
import com.projet.freelencetinder.models.ChatMessage;
import com.projet.freelencetinder.models.Conversation;
import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.repository.ChatMessageRepository;
import com.projet.freelencetinder.repository.ConversationRepository;
import com.projet.freelencetinder.repository.UtilisateurRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatMessageService {

    private final ChatMessageRepository messageRepo;
    private final ConversationRepository conversationRepo;
    private final UtilisateurRepository userRepo;
    private final SimpMessagingTemplate broker;

    public ChatMessageService(ChatMessageRepository messageRepo,
                              ConversationRepository conversationRepo,
                              UtilisateurRepository userRepo,
                              SimpMessagingTemplate broker) {
        this.messageRepo = messageRepo;
        this.conversationRepo = conversationRepo;
        this.userRepo = userRepo;
        this.broker = broker;
    }

    /* --------------------------------------------------------- */
    @Transactional
    public ChatMessageResponse sendMessage(SendMessageRequest req, Long senderId) {

        Conversation conv = conversationRepo
            .findById(req.getConversationId())
            .orElseThrow(() -> new IllegalArgumentException("Conversation inexistante"));

        Utilisateur sender = userRepo.getReferenceById(senderId);

        // Vérifie que l'expéditeur appartient à la conversation
        if (!isParticipant(conv, senderId)) {
            throw new IllegalStateException("Accès refusé");
        }

        Utilisateur receiver = sender.getId().equals(conv.getClient().getId())
            ? conv.getFreelance()
            : conv.getClient();

        ChatMessage.MessageType type = resolveType(req.getType());
        if (type == ChatMessage.MessageType.FILE && (req.getFileUrl() == null || req.getFileUrl().isBlank())) {
            throw new IllegalArgumentException("fileUrl obligatoire pour un message FILE");
        }

        ChatMessage msg = new ChatMessage();
        msg.setConversation(conv);
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setContent(req.getContent() != null ? req.getContent().trim() : "");
        msg.setType(type);
        msg.setFileUrl(req.getFileUrl());
        msg.setFileType(req.getFileType());

        ChatMessage saved = messageRepo.save(msg);

        // Mise à jour snapshot conversation
        conv.setLastMessageAt(saved.getSentAt());
        conv.setLastMessageType(saved.getType());
        conv.setLastMessageContent(saved.getContent());
        conversationRepo.save(conv);

        ChatMessageResponse resp = toDto(saved);

        // Diffusion WebSocket
        broker.convertAndSend("/topic/conversations/" + conv.getId(), resp);
        broker.convertAndSendToUser(receiver.getEmail(), "/queue/messages", resp);

        return resp;
    }

    /* --------------------------------------------------------- */
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getConversationMessages(Long conversationId,
                                                             int page, int size,
                                                             Long requesterId) {
        // Sécurité : vérifie que le demandeur est participant
        Conversation conv = conversationRepo.findById(conversationId)
            .orElseThrow(() -> new IllegalArgumentException("Conversation inexistante"));
        if (!isParticipant(conv, requesterId)) {
            throw new IllegalStateException("Accès refusé");
        }

        return messageRepo
            .findByConversationIdOrderBySentAtDesc(conversationId, PageRequest.of(page, size))
            .map(this::toDto);
    }

    /* --------------------------------------------------------- */
    @Transactional
    public void markConversationSeen(Long conversationId, Long userId) {
        Conversation conv = conversationRepo.findById(conversationId)
            .orElseThrow(() -> new IllegalArgumentException("Conversation inexistante"));
        if (!isParticipant(conv, userId)) {
            throw new IllegalStateException("Accès refusé");
        }
        messageRepo.markAllAsSeen(conversationId, userId);
    }

    /* --------------------------------------------------------- */
    private boolean isParticipant(Conversation c, Long userId) {
        return c.getClient().getId().equals(userId) || c.getFreelance().getId().equals(userId);
    }

    private ChatMessage.MessageType resolveType(String raw) {
        if (raw == null || raw.isBlank()) return ChatMessage.MessageType.TEXT;
        try {
            return ChatMessage.MessageType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ChatMessage.MessageType.TEXT;
        }
    }

    private ChatMessageResponse toDto(ChatMessage m) {
        return new ChatMessageResponse(
            m.getId(),
            m.getConversation().getId(),
            m.getSender().getId(),
            m.getReceiver().getId(),
            m.getContent(),
            m.getType().name(),
            m.getFileUrl(),
            m.getFileType(),
            m.getSentAt(),
            m.isSeen()
        );
    }
}
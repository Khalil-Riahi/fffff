package com.projet.freelencetinder.servcie;

import com.projet.freelencetinder.dto.ConversationSummaryDto;
import com.projet.freelencetinder.models.ChatMessage;
import com.projet.freelencetinder.models.Conversation;
import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.repository.ChatMessageRepository;
import com.projet.freelencetinder.repository.ConversationRepository;
import com.projet.freelencetinder.repository.MissionRepository;
import com.projet.freelencetinder.repository.UtilisateurRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepo;
    private final ChatMessageRepository messageRepo;
    private final MissionRepository missionRepo;
    private final UtilisateurRepository userRepo;

    public ConversationService(ConversationRepository conversationRepo,
                               ChatMessageRepository messageRepo,
                               MissionRepository missionRepo,
                               UtilisateurRepository userRepo) {
        this.conversationRepo = conversationRepo;
        this.messageRepo = messageRepo;
        this.missionRepo = missionRepo;
        this.userRepo = userRepo;
    }

    /* -------------------------------------------------------------------- */
    @Transactional
    public Conversation findOrCreate(Long missionId, Long clientId, Long freelanceId) {

        return conversationRepo
            .findByMissionIdAndClientIdAndFreelanceId(missionId, clientId, freelanceId)
            .orElseGet(() -> {
                Mission mission = missionRepo.getReferenceById(missionId);
                Utilisateur client = userRepo.getReferenceById(clientId);
                Utilisateur free = userRepo.getReferenceById(freelanceId);

                // TODO: vérifier autorisation métier (match valide)
                Conversation c = new Conversation();
                c.setMission(mission);
                c.setClient(client);
                c.setFreelance(free);
                try {
                    return conversationRepo.save(c);
                } catch (DataIntegrityViolationException e) {
                    // Conflit car créée simultanément par un autre thread
                    return conversationRepo
                        .findByMissionIdAndClientIdAndFreelanceId(missionId, clientId, freelanceId)
                        .orElseThrow();
                }
            });
    }

    /* -------------------------------------------------------------------- */
    @Transactional(readOnly = true)
    public List<ConversationSummaryDto> getUserConversations(Long userId, int page, int size) {

        return conversationRepo
            .findByClientIdOrFreelanceIdOrderByLastMessageAtDesc(
                userId, userId, PageRequest.of(page, size))
            .stream()
            .map(c -> {
                boolean isClient = c.getClient().getId().equals(userId);
                Utilisateur other = isClient ? c.getFreelance() : c.getClient();

                long unread = messageRepo
                    .countByConversationIdAndReceiverIdAndSeenFalse(c.getId(), userId);

                String preview = buildPreview(c.getLastMessageContent(), c.getLastMessageType());

                return new ConversationSummaryDto(
                    c.getId(),
                    c.getMission().getId(),
                    c.getMission().getTitre(),
                    other.getId(),
                    other.getNomComplet(),
                    other.getPhotoProfilUrl(),
                    preview,
                    c.getLastMessageType() != null ? c.getLastMessageType().name() : null,
                    c.getLastMessageAt(),
                    (int) unread
                );
            })
            .collect(Collectors.toList());
    }

    /* -------------------------------------------------------------------- */
    private String buildPreview(String content, ChatMessage.MessageType type) {
        if (type == null) return null;
        if (type == ChatMessage.MessageType.FILE) return "[Fichier]";
        if (type == ChatMessage.MessageType.LINK) return "[Lien] " + safeTruncate(content, 80);
        return safeTruncate(content, 120);
    }

    private String safeTruncate(String txt, int max) {
        if (txt == null) return null;
        return txt.length() <= max ? txt : txt.substring(0, max - 3) + "...";
    }
}

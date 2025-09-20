package com.projet.freelencetinder.servcie;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.projet.freelencetinder.dto.*;
import com.projet.freelencetinder.models.Notification;
import com.projet.freelencetinder.models.Notification.*;

import com.projet.freelencetinder.repository.NotificationRepository;
import com.projet.freelencetinder.repository.UtilisateurRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class NotificationService {

    private final NotificationRepository repo;
    private final UtilisateurRepository  userRepo;
    private final SimpMessagingTemplate  broker;

    public NotificationService(NotificationRepository repo,
                               UtilisateurRepository userRepo,
                               SimpMessagingTemplate broker) {
        this.repo     = repo;
        this.userRepo = userRepo;
        this.broker   = broker;
    }

    /* =============================================================== */
    @Transactional
    public Notification push(NotificationType type,
                             Long recipientId,
                             Long senderId,
                             Long referenceId,
                             String title,
                             String body,
                             Priority priority,
                             Channel channel,
                             Map<String,Object> data,
                             LocalDateTime expiresAt) {

        Notification n = new Notification();
        n.setRecipient(userRepo.getReferenceById(recipientId));
        if (senderId != null) n.setSender(userRepo.getReferenceById(senderId));
        n.setType(type);
        n.setReferenceId(referenceId);
        n.setTitle(title);
        n.setBody(body);
        n.setPriority(priority == null ? Priority.NORMAL : priority);
        n.setChannel(channel == null ? Channel.WEB    : channel);
        n.setData(data);
        n.setExpiresAt(expiresAt);

        repo.save(n);

        /* ---- payload temps réel ---- */
        NotificationSocketDTO dto = new NotificationSocketDTO();
        dto.setId(n.getId());
        dto.setType(n.getType());
        dto.setTitle(n.getTitle());
        dto.setBody(n.getBody());
        dto.setPriority(n.getPriority());
        dto.setChannel(n.getChannel());
        dto.setReferenceId(n.getReferenceId());
        dto.setCreatedAt(n.getCreatedAt());
        dto.setData(n.getData());

        broker.convertAndSendToUser(
            n.getRecipient().getEmail(),
            "/queue/notifications",
            dto
        );

        return n;
    }

    /* =============================================================== */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> latest(Long userId, int page, int size) {
        return repo.findByRecipientIdAndArchivedFalseOrderByCreatedAtDesc(
                    userId, PageRequest.of(page, size))
                   .map(this::toDto);
    }

    @Transactional
    public void markSeen(Long notifId, Long userId) {
        Notification n = repo.findById(notifId)
              .orElseThrow(() -> new EntityNotFoundException("Notif introuvable"));
        if (!n.getRecipient().getId().equals(userId))
            throw new IllegalStateException("Pas votre notification !");
        n.setSeen(true);
    }

    @Transactional
    public void markAllSeen(Long userId) { repo.markAllSeen(userId); }

    @Transactional
    public void archiveSeen(Long userId) { repo.archiveAllSeen(userId); }

    /* =============================================================== */
    private NotificationDTO toDto(Notification n) {
        NotificationDTO d = new NotificationDTO();
        d.setId(n.getId());
        d.setType(n.getType());
        d.setTitle(n.getTitle());
        d.setBody(n.getBody());
        d.setSeen(n.isSeen());
        d.setArchived(n.isArchived());
        d.setPriority(n.getPriority());
        d.setChannel(n.getChannel());
        d.setCreatedAt(n.getCreatedAt());
        d.setExpiresAt(n.getExpiresAt());
        d.setReferenceId(n.getReferenceId());
        d.setData(n.getData());
        return d;
    }
}

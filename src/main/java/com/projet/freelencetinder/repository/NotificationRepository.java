package com.projet.freelencetinder.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.projet.freelencetinder.models.Notification;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {

    /**
     * Maintenant renvoie une Page<Notification> au lieu d’une List<Notification>.
     */
    Page<Notification> findByRecipientIdAndArchivedFalseOrderByCreatedAtDesc(
            Long recipientId,
            Pageable pageable);

    long countByRecipientIdAndSeenFalseAndArchivedFalse(Long recipientId);

    @Modifying
    @Query("update Notification n set n.seen = true where n.recipient.id = :uid and n.seen = false")
    void markAllSeen(@Param("uid") Long uid);

    @Modifying
    @Query("update Notification n set n.archived = true where n.recipient.id = :uid and n.archived = false and n.seen = true")
    void archiveAllSeen(@Param("uid") Long uid);
}

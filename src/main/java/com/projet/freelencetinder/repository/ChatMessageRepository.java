package com.projet.freelencetinder.repository;

import com.projet.freelencetinder.models.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import jakarta.transaction.Transactional;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByConversationIdOrderBySentAtDesc(Long conversationId, Pageable pageable);

    long countByConversationIdAndReceiverIdAndSeenFalse(Long conversationId, Long receiverId);

    @Modifying
    @Transactional
    @Query("update ChatMessage m set m.seen = true " +
           "where m.conversation.id = :convId and m.receiver.id = :userId and m.seen = false")
    int markAllAsSeen(@Param("convId") Long conversationId, @Param("userId") Long userId);
}

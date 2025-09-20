package com.projet.freelencetinder.repository;

import java.util.List;
import java.util.Optional;

import com.projet.freelencetinder.models.Conversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByMissionIdAndClientIdAndFreelanceId(Long missionId,
                                                                    Long clientId,
                                                                    Long freelanceId);

    List<Conversation> findByClientIdOrFreelanceIdOrderByLastMessageAtDesc(Long clientId,
                                                                           Long freelanceId,
                                                                           Pageable pageable);
}

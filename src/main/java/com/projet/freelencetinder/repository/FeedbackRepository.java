package com.projet.freelencetinder.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.projet.freelencetinder.models.Feedback;
import com.projet.freelencetinder.models.FeedbackRole;
import com.projet.freelencetinder.models.FeedbackStatus;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    boolean existsByMissionIdAndIssuerIdAndTargetIdAndRoleAndStatusNot(Long missionId,
                                                                        Long issuerId,
                                                                        Long targetId,
                                                                        FeedbackRole role,
                                                                        FeedbackStatus status);

    Page<Feedback> findByMissionId(Long missionId, Pageable pageable);

    @Query("select f from Feedback f where f.targetId = :targetId and f.status = com.projet.freelencetinder.models.FeedbackStatus.PUBLISHED and (:start is null or f.publishedAt >= :start) and (:end is null or f.publishedAt <= :end)")
    Page<Feedback> findPublishedByTarget(Long targetId, Instant start, Instant end, Pageable pageable);

    boolean existsByMissionIdAndIssuerIdAndTargetIdAndRoleAndIdempotencyKey(Long missionId,
                                                                            Long issuerId,
                                                                            Long targetId,
                                                                            FeedbackRole role,
                                                                            String idempotencyKey);

    Optional<Feedback> findByMissionIdAndIssuerId(Long missionId, Long issuerId);
}



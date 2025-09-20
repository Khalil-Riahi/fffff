package com.projet.freelencetinder.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.projet.freelencetinder.models.FeedbackAggregate;

@Repository
public interface FeedbackAggregateRepository extends JpaRepository<FeedbackAggregate, Long> {
    Optional<FeedbackAggregate> findByTargetIdAndWindowDays(Long targetId, int windowDays);
}



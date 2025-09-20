package com.projet.freelencetinder.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.projet.freelencetinder.models.FeedbackWindow;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

@Repository
public interface FeedbackWindowRepository extends JpaRepository<FeedbackWindow, Long> {

    Optional<FeedbackWindow> findByMissionId(Long missionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from FeedbackWindow w where w.missionId = :missionId")
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    Optional<FeedbackWindow> findByMissionIdForUpdate(@Param("missionId") Long missionId);
}



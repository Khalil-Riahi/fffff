// SwipeRepository.java
package com.projet.freelencetinder.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projet.freelencetinder.models.Swipe;

public interface SwipeRepository extends JpaRepository<Swipe, Long> {

    Optional<Swipe> findByFreelanceIdAndMissionId(Long freelanceId, Long missionId);

    List<Swipe> findByFreelanceId(Long freelanceId);
    
    List<Swipe> findByFreelanceIdAndDecision(Long freelanceId, Swipe.Decision decision);
    
    

    /* NOUVEAU ----------------------------------------------------------------- */
    /** Tous les swipes LIKE d’une mission (côté freelances). */
    List<Swipe> findByMissionIdAndDecision(Long missionId, Swipe.Decision decision);
    
}

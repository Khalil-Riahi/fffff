// ClientSwipeRepository.java
package com.projet.freelencetinder.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projet.freelencetinder.models.ClientSwipe;

public interface ClientSwipeRepository extends JpaRepository<ClientSwipe, Long> {

    Optional<ClientSwipe> findByClientIdAndMissionIdAndFreelanceId(
            Long clientId, Long missionId, Long freelanceId);
    
    List<ClientSwipe> findByClientIdAndMissionId(Long clientId, Long missionId);

}

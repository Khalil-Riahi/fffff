// MissionRepository.java
package com.projet.freelencetinder.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import com.projet.freelencetinder.models.Mission;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {
    // CRUD de base fourni par JpaRepository
	
	/* Méthodes custom ajoutables dans MissionRepository */

	@EntityGraph(attributePaths = {"freelanceSelectionne", "client"})
	List<Mission> findByClientId(Long clientId);
	
	
	List<Mission> findByStatutAndFreelanceSelectionneIsNotNull(Mission.Statut statut);


}

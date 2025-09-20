package com.projet.freelencetinder.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.projet.freelencetinder.models.Utilisateur;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    boolean existsByEmail(String email);
    Optional<Utilisateur> findByEmail(String email);
    
    
    
    /* Méthodes custom ajoutables dans UtilisateurRepository */
    @Query("""
        SELECT COALESCE(SUM(t.montantNetFreelance),0)
        FROM TranchePaiement t
        WHERE t.freelance.id = :freelanceId
          AND t.statut = 'VERSEE_FREELANCE'
    """)
    java.math.BigDecimal totalGagneParFreelance(Long freelanceId);

}

package com.projet.freelencetinder.repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.projet.freelencetinder.models.TranchePaiement;
import com.projet.freelencetinder.models.TranchePaiement.StatutTranche;

import jakarta.persistence.LockModeType;

@Repository
public interface TranchePaiementRepository extends JpaRepository<TranchePaiement, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TranchePaiement t where t.id = :id")
    Optional<TranchePaiement> findByIdForUpdate(@Param("id") Long id);

    List<TranchePaiement> findByMissionIdOrderByOrdreAsc(Long missionId);

    Optional<TranchePaiement> findByPaymeeCheckoutId(String paymeeCheckoutId);

    List<TranchePaiement> findByStatut(StatutTranche statut);

    Optional<TranchePaiement> findFirstByMissionIdAndStatutOrderByOrdreAsc(Long missionId, StatutTranche statut);

    Optional<TranchePaiement> findFirstByMissionIdAndStatutInOrderByOrdreAsc(Long missionId, Collection<StatutTranche> statuts);

    Optional<TranchePaiement> findByLivrableAssocieId(Long livrableId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
     select t from TranchePaiement t
     where t.mission.id = :missionId
       and t.statut = :statut
       and t.ordre = (
         select min(t2.ordre) from TranchePaiement t2
         where t2.mission.id = :missionId and t2.statut = :statut
       )
    """)
    Optional<TranchePaiement> findNextForUpdate(@Param("missionId") Long missionId, @Param("statut") StatutTranche statut);

    /* ===== Agrégation tranche due pour cartes ===== */
    public interface TrancheDue {
        Long getMissionId();
        Long getTrancheIdDue();
    }

    @Query("""
      select t.mission.id as missionId,
             min(case when t.statut = com.projet.freelencetinder.models.TranchePaiement$StatutTranche.EN_ATTENTE_PAIEMENT
                      then t.id else null end) as trancheIdDue
      from TranchePaiement t
      where t.mission.id in :missionIds
      group by t.mission.id
    """)
    List<TrancheDue> trancheDueByMissionIds(@Param("missionIds") List<Long> missionIds);

}

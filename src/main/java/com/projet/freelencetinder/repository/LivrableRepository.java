package com.projet.freelencetinder.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.projet.freelencetinder.models.Livrable;
import com.projet.freelencetinder.models.StatusLivrable;

@Repository
public interface LivrableRepository extends JpaRepository<Livrable, Long> {

    /* Tous les livrables d’une mission (tri configurable) */
    List<Livrable> findByMissionId(Long missionId, Sort sort);

    /* Tous les livrables d’une mission ordonnés par date d'envoi (desc) */
    List<Livrable> findByMissionIdOrderByDateEnvoiDesc(Long missionId);

    /* Tous les livrables d’un freelance (facultatif) */
    List<Livrable> findByFreelancerId(Long freelancerId, Sort sort);

    /* Filtrer par statut */
    List<Livrable> findByMissionIdAndStatus(Long missionId, StatusLivrable status, Sort sort);

    /* ===== Agrégations pour KPI cartes ===== */
    public interface LivrableAgg {
        Long getMissionId();
        Long getTotal();
        Long getValides();
        Long getEnAttente();
    }

    @Query("""
      select l.mission.id as missionId,
             count(l) as total,
             sum(case when l.status = com.projet.freelencetinder.models.StatusLivrable.VALIDE then 1 else 0 end) as valides,
             sum(case when l.status = com.projet.freelencetinder.models.StatusLivrable.EN_ATTENTE then 1 else 0 end) as enAttente
      from Livrable l
      where l.mission.id in :missionIds
      group by l.mission.id
    """)
    List<LivrableAgg> aggregateByMissionIds(@Param("missionIds") List<Long> missionIds);
}

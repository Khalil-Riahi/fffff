package com.projet.freelencetinder.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.projet.freelencetinder.models.WithdrawalMethod;

import jakarta.persistence.LockModeType;

@Repository
public interface WithdrawalMethodRepository extends JpaRepository<WithdrawalMethod, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from WithdrawalMethod w where w.freelance.id = :freelanceId")
    List<WithdrawalMethod> findByFreelanceIdForUpdate(@Param("freelanceId") Long freelanceId);

    @Query("select w from WithdrawalMethod w where w.freelance.id = :freelanceId")
    List<WithdrawalMethod> findByFreelanceId(@Param("freelanceId") Long freelanceId);

    Optional<WithdrawalMethod> findByFreelanceIdAndPrincipalTrue(Long freelanceId);
}

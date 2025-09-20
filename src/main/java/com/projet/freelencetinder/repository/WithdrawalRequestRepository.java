package com.projet.freelencetinder.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.projet.freelencetinder.models.WithdrawalRequest;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {

    List<WithdrawalRequest> findByFreelanceIdOrderByDateDemandeDesc(Long freelanceId);
}

package com.projet.freelencetinder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.projet.freelencetinder.models.PaymentAudit;

public interface PaymentAuditRepository extends JpaRepository<PaymentAudit, Long> {
}

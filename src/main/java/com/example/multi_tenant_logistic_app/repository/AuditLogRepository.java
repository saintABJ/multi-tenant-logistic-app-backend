package com.example.multi_tenant_logistic_app.repository;

import com.example.multi_tenant_logistic_app.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByCompanyId(Long companyId);
}

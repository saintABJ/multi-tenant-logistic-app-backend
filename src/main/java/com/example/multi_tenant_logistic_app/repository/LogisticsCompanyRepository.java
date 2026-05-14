package com.example.multi_tenant_logistic_app.repository;

import com.example.multi_tenant_logistic_app.domain.entity.LogisticsCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LogisticsCompanyRepository extends JpaRepository<LogisticsCompany, Long> {
    Optional<LogisticsCompany> findByEmail(String email);
    Optional<LogisticsCompany> findByRcNumber(String rcNumber);
}

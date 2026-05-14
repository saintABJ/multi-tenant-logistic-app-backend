package com.example.multi_tenant_logistic_app.repository;

import com.example.multi_tenant_logistic_app.domain.entity.Invoice;
import com.example.multi_tenant_logistic_app.domain.enumeration.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByShipmentId(Long shipmentId);
    List<Invoice> findByCustomerId(Long customerId);
    List<Invoice> findByCompanyId(Long companyId);
    List<Invoice> findByCompanyIdAndStatusAndCreatedAtBefore(Long companyId, InvoiceStatus status, LocalDateTime dateTime);
}

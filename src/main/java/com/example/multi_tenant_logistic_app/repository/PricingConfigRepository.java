package com.example.multi_tenant_logistic_app.repository;

import com.example.multi_tenant_logistic_app.domain.entity.PricingConfig;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PricingConfigRepository extends JpaRepository<PricingConfig, Long> {
    Optional<PricingConfig> findByCompanyIdAndShipmentType(Long companyId, ShipmentType shipmentType);
}

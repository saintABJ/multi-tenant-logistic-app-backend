package com.example.multi_tenant_logistic_app.service;

import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentType;

import java.math.BigDecimal;

public interface PricingService {
    BigDecimal computeFee(Long companyId, ShipmentType shipmentType, BigDecimal weightKg, BigDecimal declaredValue);
}

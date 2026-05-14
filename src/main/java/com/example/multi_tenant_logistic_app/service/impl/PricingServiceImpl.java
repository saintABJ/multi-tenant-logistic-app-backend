package com.example.multi_tenant_logistic_app.service.impl;

import com.example.multi_tenant_logistic_app.domain.entity.PricingConfig;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentType;
import com.example.multi_tenant_logistic_app.repository.PricingConfigRepository;
import com.example.multi_tenant_logistic_app.service.PricingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingServiceImpl implements PricingService {

    private final PricingConfigRepository pricingConfigRepository;

    public PricingServiceImpl(PricingConfigRepository pricingConfigRepository) {
        this.pricingConfigRepository = pricingConfigRepository;
    }

    @Override
    public BigDecimal computeFee(Long companyId, ShipmentType shipmentType, BigDecimal weightKg, BigDecimal declaredValue) {
        PricingConfig config = pricingConfigRepository.findByCompanyIdAndShipmentType(companyId, shipmentType)
                .orElseThrow(() -> new RuntimeException("Pricing config not found for company and shipment type"));

        BigDecimal baseFee = config.getBaseFeePerKg().multiply(weightKg);
        BigDecimal insuranceFee = BigDecimal.ZERO;

        if (declaredValue != null && config.getInsuranceThreshold() != null 
                && declaredValue.compareTo(config.getInsuranceThreshold()) > 0) {
            insuranceFee = config.getInsuranceFee() != null ? config.getInsuranceFee() : BigDecimal.ZERO;
        }

        return baseFee.add(insuranceFee);
    }
}

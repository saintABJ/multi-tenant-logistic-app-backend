package com.example.multi_tenant_logistic_app.service;

import com.example.multi_tenant_logistic_app.domain.entity.PricingConfig;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentType;
import com.example.multi_tenant_logistic_app.repository.PricingConfigRepository;
import com.example.multi_tenant_logistic_app.service.impl.PricingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PricingServiceTest {

    @Mock
    private PricingConfigRepository pricingConfigRepository;

    @InjectMocks
    private PricingServiceImpl pricingService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testComputeFee_WithInsurance() {
        Long companyId = 1L;
        ShipmentType type = ShipmentType.EXPRESS;
        BigDecimal weight = BigDecimal.valueOf(10.0);
        BigDecimal declaredValue = BigDecimal.valueOf(1000.0);

        PricingConfig config = PricingConfig.builder()
                .baseFeePerKg(BigDecimal.valueOf(5.0))
                .insuranceThreshold(BigDecimal.valueOf(500.0))
                .insuranceFee(BigDecimal.valueOf(50.0))
                .build();

        when(pricingConfigRepository.findByCompanyIdAndShipmentType(companyId, type))
                .thenReturn(Optional.of(config));

        BigDecimal fee = pricingService.computeFee(companyId, type, weight, declaredValue);

        // baseFee = 5 * 10 = 50
        // insuranceFee = 50 (declaredValue > threshold)
        // total = 100
        assertEquals(0, fee.compareTo(BigDecimal.valueOf(100.0)));
    }

    @Test
    public void testComputeFee_WithoutInsurance() {
        Long companyId = 1L;
        ShipmentType type = ShipmentType.EXPRESS;
        BigDecimal weight = BigDecimal.valueOf(10.0);
        BigDecimal declaredValue = BigDecimal.valueOf(100.0);

        PricingConfig config = PricingConfig.builder()
                .baseFeePerKg(BigDecimal.valueOf(5.0))
                .insuranceThreshold(BigDecimal.valueOf(500.0))
                .insuranceFee(BigDecimal.valueOf(50.0))
                .build();

        when(pricingConfigRepository.findByCompanyIdAndShipmentType(companyId, type))
                .thenReturn(Optional.of(config));

        BigDecimal fee = pricingService.computeFee(companyId, type, weight, declaredValue);

        // baseFee = 5 * 10 = 50
        // insuranceFee = 0 (declaredValue < threshold)
        // total = 50
        assertEquals(0, fee.compareTo(BigDecimal.valueOf(50.0)));
    }

    @Test
    public void testComputeFee_ConfigNotFound() {
        Long companyId = 1L;
        ShipmentType type = ShipmentType.EXPRESS;
        BigDecimal weight = BigDecimal.valueOf(10.0);
        BigDecimal declaredValue = BigDecimal.valueOf(100.0);

        when(pricingConfigRepository.findByCompanyIdAndShipmentType(companyId, type))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
                pricingService.computeFee(companyId, type, weight, declaredValue));
    }
}

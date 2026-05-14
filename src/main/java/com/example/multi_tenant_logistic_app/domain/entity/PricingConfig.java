package com.example.multi_tenant_logistic_app.domain.entity;

import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pricing_configs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "shipment_type"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private LogisticsCompany company;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_type", nullable = false)
    private ShipmentType shipmentType;

    @Column(name = "base_fee_per_kg", nullable = false)
    private BigDecimal baseFeePerKg;

    @Column(name = "insurance_threshold")
    private BigDecimal insuranceThreshold;

    @Column(name = "insurance_fee")
    private BigDecimal insuranceFee;
}

package com.example.multi_tenant_logistic_app.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class RevenueReportResponse {
    private String shipmentType;
    private Integer month;
    private BigDecimal totalFees;
}

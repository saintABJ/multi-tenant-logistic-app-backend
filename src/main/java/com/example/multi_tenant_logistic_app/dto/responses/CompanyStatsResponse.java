package com.example.multi_tenant_logistic_app.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CompanyStatsResponse {
    private Long companyId;
    private String companyName;
    private Long shipmentVolume;
    private BigDecimal revenue;
}

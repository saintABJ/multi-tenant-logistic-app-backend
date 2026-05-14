package com.example.multi_tenant_logistic_app.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlatformStatsResponse {
    private Long totalShipmentsToday;
    private Long totalDeliveredThisMonth;
    private Double averageDeliveryTimeHours;
}

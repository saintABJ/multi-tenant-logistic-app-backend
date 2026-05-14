package com.example.multi_tenant_logistic_app.repository.projection;

import java.math.BigDecimal;

public interface RevenueReportProjection {
    String getShipmentType();
    Integer getMonth();
    BigDecimal getTotalFees();
}

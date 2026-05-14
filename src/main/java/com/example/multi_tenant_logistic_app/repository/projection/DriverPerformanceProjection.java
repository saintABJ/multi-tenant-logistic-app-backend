package com.example.multi_tenant_logistic_app.repository.projection;

import java.math.BigDecimal;

public interface DriverPerformanceProjection {
    Long getDriverId();
    String getName();
    BigDecimal getRating();
    Integer getTotalDeliveries();
    Double getOnTimeRate();
}

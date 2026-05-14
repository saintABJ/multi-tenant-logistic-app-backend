package com.example.multi_tenant_logistic_app.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DriverResponse {
    private Long driverId;
    private String name;
    private String email;
    private BigDecimal rating;
    private Integer totalDeliveries;
    private Long currentAssignmentId;
    private com.example.multi_tenant_logistic_app.domain.enumeration.AssignmentStatus currentAssignmentStatus;
}

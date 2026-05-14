package com.example.multi_tenant_logistic_app.dto.requests;

import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ShipmentStatus status;

    private String note;
    private String locationCity;
    private String locationState;
}

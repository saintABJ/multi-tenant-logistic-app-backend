package com.example.multi_tenant_logistic_app.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignShipmentRequest {

    @NotNull(message = "Shipment ID is required")
    private Long shipmentId;

    @NotNull(message = "Driver ID is required")
    private Long driverId;

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;
}

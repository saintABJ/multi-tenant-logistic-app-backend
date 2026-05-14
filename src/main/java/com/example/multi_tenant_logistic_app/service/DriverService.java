package com.example.multi_tenant_logistic_app.service;

import com.example.multi_tenant_logistic_app.domain.entity.DeliveryAssignment;
import com.example.multi_tenant_logistic_app.dto.requests.DriverStatusUpdateRequest;

import java.util.Optional;

public interface DriverService {
    Optional<DeliveryAssignment> getActiveAssignment(Long driverId);
    void updateShipmentStatus(Long driverId, Long shipmentId, DriverStatusUpdateRequest request);
}

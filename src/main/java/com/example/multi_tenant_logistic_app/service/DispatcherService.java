package com.example.multi_tenant_logistic_app.service;

import com.example.multi_tenant_logistic_app.domain.entity.DeliveryAssignment;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus;
import com.example.multi_tenant_logistic_app.dto.requests.AssignShipmentRequest;

public interface DispatcherService {
    DeliveryAssignment assignShipment(Long dispatcherId, AssignShipmentRequest request);
    DeliveryAssignment reassignShipment(Long dispatcherId, Long currentAssignmentId, AssignShipmentRequest request);
    void updateShipmentStatus(Long dispatcherId, Long shipmentId, ShipmentStatus status, String note, String locationCity, String locationState);
}

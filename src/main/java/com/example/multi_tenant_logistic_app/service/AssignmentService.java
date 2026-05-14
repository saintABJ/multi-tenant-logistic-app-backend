package com.example.multi_tenant_logistic_app.service;

import com.example.multi_tenant_logistic_app.domain.entity.DeliveryAssignment;
import com.example.multi_tenant_logistic_app.dto.requests.AssignShipmentRequest;

public interface AssignmentService {
    DeliveryAssignment assignShipment(Long dispatcherId, AssignShipmentRequest request);
    DeliveryAssignment reassignShipment(Long dispatcherId, Long currentAssignmentId, AssignShipmentRequest request);
}

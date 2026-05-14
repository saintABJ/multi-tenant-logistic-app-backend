package com.example.multi_tenant_logistic_app.service.impl;

import com.example.multi_tenant_logistic_app.domain.entity.DeliveryAssignment;
import com.example.multi_tenant_logistic_app.domain.entity.Shipment;
import com.example.multi_tenant_logistic_app.domain.entity.ShipmentStatusUpdate;
import com.example.multi_tenant_logistic_app.domain.entity.User;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus;
import com.example.multi_tenant_logistic_app.dto.requests.AssignShipmentRequest;
import com.example.multi_tenant_logistic_app.exception.BadRequestException;
import com.example.multi_tenant_logistic_app.exception.ResourceNotFoundException;
import com.example.multi_tenant_logistic_app.repository.ShipmentRepository;
import com.example.multi_tenant_logistic_app.repository.ShipmentStatusUpdateRepository;
import com.example.multi_tenant_logistic_app.repository.UserRepository;
import com.example.multi_tenant_logistic_app.service.AssignmentService;
import com.example.multi_tenant_logistic_app.service.DispatcherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

@Service
public class DispatcherServiceImpl implements DispatcherService {

    private final AssignmentService assignmentService;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentStatusUpdateRepository statusUpdateRepository;
    private final UserRepository userRepository;

    public DispatcherServiceImpl(AssignmentService assignmentService,
                                 ShipmentRepository shipmentRepository,
                                 ShipmentStatusUpdateRepository statusUpdateRepository,
                                 UserRepository userRepository) {
        this.assignmentService = assignmentService;
        this.shipmentRepository = shipmentRepository;
        this.statusUpdateRepository = statusUpdateRepository;
        this.userRepository = userRepository;
    }

    @Override
    public DeliveryAssignment assignShipment(Long dispatcherId, AssignShipmentRequest request) {
        return assignmentService.assignShipment(dispatcherId, request);
    }

    @Override
    public DeliveryAssignment reassignShipment(Long dispatcherId, Long currentAssignmentId, AssignShipmentRequest request) {
        return assignmentService.reassignShipment(dispatcherId, currentAssignmentId, request);
    }

    @Override
    @Transactional
    public void updateShipmentStatus(Long dispatcherId, Long shipmentId, ShipmentStatus status, String note, String locationCity, String locationState) {
        // Validate status
        EnumSet<ShipmentStatus> allowedStatuses = EnumSet.of(ShipmentStatus.PICKED_UP, ShipmentStatus.IN_WAREHOUSE);
        if (!allowedStatuses.contains(status)) {
            throw new BadRequestException("Dispatcher is only allowed to update status to PICKED_UP or IN_WAREHOUSE");
        }

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));
        User dispatcher = userRepository.findById(dispatcherId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatcher not found"));

        // Update shipment status
        shipment.setStatus(status);
        shipmentRepository.save(shipment);

        // Create status update log
        ShipmentStatusUpdate statusUpdate = ShipmentStatusUpdate.builder()
                .shipment(shipment)
                .status(status)
                .note(note)
                .locationCity(locationCity)
                .locationState(locationState)
                .updatedBy(dispatcher)
                .build();

        statusUpdateRepository.save(statusUpdate);
    }
}

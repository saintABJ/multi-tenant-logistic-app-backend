package com.example.multi_tenant_logistic_app.service.impl;

import com.example.multi_tenant_logistic_app.domain.entity.*;
import com.example.multi_tenant_logistic_app.domain.enumeration.AssignmentStatus;
import com.example.multi_tenant_logistic_app.domain.enumeration.VehicleStatus;
import com.example.multi_tenant_logistic_app.dto.requests.AssignShipmentRequest;
import com.example.multi_tenant_logistic_app.exception.BadRequestException;
import com.example.multi_tenant_logistic_app.exception.ResourceNotFoundException;
import com.example.multi_tenant_logistic_app.repository.DeliveryAssignmentRepository;
import com.example.multi_tenant_logistic_app.repository.ShipmentRepository;
import com.example.multi_tenant_logistic_app.repository.UserRepository;
import com.example.multi_tenant_logistic_app.repository.VehicleRepository;
import com.example.multi_tenant_logistic_app.service.AssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final DeliveryAssignmentRepository assignmentRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    public AssignmentServiceImpl(DeliveryAssignmentRepository assignmentRepository,
                                 ShipmentRepository shipmentRepository,
                                 UserRepository userRepository,
                                 VehicleRepository vehicleRepository) {
        this.assignmentRepository = assignmentRepository;
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional
    public DeliveryAssignment assignShipment(Long dispatcherId, AssignShipmentRequest request) {
        Shipment shipment = shipmentRepository.findById(request.getShipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));
        User driver = userRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        User dispatcher = userRepository.findById(dispatcherId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatcher not found"));

        // Check vehicle availability
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new BadRequestException("Vehicle is not available. Current status: " + vehicle.getStatus());
        }

        // Check driver availability (no ACTIVE assignment)
        List<DeliveryAssignment> activeAssignments = assignmentRepository.findByDriverIdAndStatus(driver.getId(), AssignmentStatus.ACTIVE);
        if (!activeAssignments.isEmpty()) {
            throw new BadRequestException("Driver already has an active assignment");
        }

        // Create assignment
        DeliveryAssignment assignment = DeliveryAssignment.builder()
                .shipment(shipment)
                .driver(driver)
                .vehicle(vehicle)
                .assignedBy(dispatcher)
                .status(AssignmentStatus.ACTIVE)
                .build();

        // Update vehicle status
        vehicle.setStatus(VehicleStatus.ON_DELIVERY);
        vehicleRepository.save(vehicle);

        return assignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public DeliveryAssignment reassignShipment(Long dispatcherId, Long currentAssignmentId, AssignShipmentRequest request) {
        DeliveryAssignment currentAssignment = assignmentRepository.findById(currentAssignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Current assignment not found"));

        if (currentAssignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new BadRequestException("Only active assignments can be reassigned");
        }

        // Mark old assignment as REASSIGNED
        currentAssignment.setStatus(AssignmentStatus.REASSIGNED);
        assignmentRepository.save(currentAssignment);

        // Reset old vehicle status if needed (assuming it's the same vehicle or a different one)
        Vehicle oldVehicle = currentAssignment.getVehicle();
        oldVehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(oldVehicle);

        // Create new assignment
        return assignShipment(dispatcherId, request);
    }
}

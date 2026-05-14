package com.example.multi_tenant_logistic_app.service.impl;

import com.example.multi_tenant_logistic_app.domain.entity.*;
import com.example.multi_tenant_logistic_app.domain.enumeration.AssignmentStatus;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus;
import com.example.multi_tenant_logistic_app.domain.enumeration.VehicleStatus;
import com.example.multi_tenant_logistic_app.dto.requests.DriverStatusUpdateRequest;
import com.example.multi_tenant_logistic_app.exception.BadRequestException;
import com.example.multi_tenant_logistic_app.exception.ResourceNotFoundException;
import com.example.multi_tenant_logistic_app.exception.UnauthorizedAccessException;
import com.example.multi_tenant_logistic_app.repository.*;
import com.example.multi_tenant_logistic_app.service.DriverService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Service
public class DriverServiceImpl implements DriverService {

    private final DeliveryAssignmentRepository assignmentRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentStatusUpdateRepository statusUpdateRepository;
    private final ProofOfDeliveryRepository proofOfDeliveryRepository;
    private final FailedDeliveryAttemptRepository failedDeliveryAttemptRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    public DriverServiceImpl(DeliveryAssignmentRepository assignmentRepository,
                             ShipmentRepository shipmentRepository,
                             ShipmentStatusUpdateRepository statusUpdateRepository,
                             ProofOfDeliveryRepository proofOfDeliveryRepository,
                             FailedDeliveryAttemptRepository failedDeliveryAttemptRepository,
                             UserRepository userRepository,
                             VehicleRepository vehicleRepository) {
        this.assignmentRepository = assignmentRepository;
        this.shipmentRepository = shipmentRepository;
        this.statusUpdateRepository = statusUpdateRepository;
        this.proofOfDeliveryRepository = proofOfDeliveryRepository;
        this.failedDeliveryAttemptRepository = failedDeliveryAttemptRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Optional<DeliveryAssignment> getActiveAssignment(Long driverId) {
        List<DeliveryAssignment> assignments = assignmentRepository.findByDriverIdAndStatus(driverId, AssignmentStatus.ACTIVE);
        return assignments.stream().findFirst();
    }

    @Override
    @Transactional
    public void updateShipmentStatus(Long driverId, Long shipmentId, DriverStatusUpdateRequest request) {
        // Validate status
        EnumSet<ShipmentStatus> allowedStatuses = EnumSet.of(
                ShipmentStatus.IN_TRANSIT,
                ShipmentStatus.OUT_FOR_DELIVERY,
                ShipmentStatus.DELIVERED,
                ShipmentStatus.FAILED_DELIVERY
        );
        if (!allowedStatuses.contains(request.getStatus())) {
            throw new BadRequestException("Driver is not allowed to update status to " + request.getStatus());
        }

        // Verify active assignment
        DeliveryAssignment assignment = getActiveAssignment(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("No active assignment found for driver"));

        if (!assignment.getShipment().getId().equals(shipmentId)) {
            throw new UnauthorizedAccessException("Shipment does not belong to driver's active assignment");
        }

        Shipment shipment = assignment.getShipment();
        User driver = assignment.getDriver();

        // Update shipment status
        shipment.setStatus(request.getStatus());
        
        if (request.getStatus() == ShipmentStatus.DELIVERED) {
            shipment.setActualDeliveryAt(LocalDateTime.now());
        }
        
        shipmentRepository.save(shipment);

        // Create status update log
        ShipmentStatusUpdate statusUpdate = ShipmentStatusUpdate.builder()
                .shipment(shipment)
                .status(request.getStatus())
                .note(request.getNote())
                .locationCity(request.getLocationCity())
                .locationState(request.getLocationState())
                .updatedBy(driver)
                .build();

        statusUpdateRepository.save(statusUpdate);

        // Handle specific statuses
        if (request.getStatus() == ShipmentStatus.DELIVERED) {
            if (request.getProofData() == null) {
                throw new BadRequestException("Proof of delivery is required for DELIVERED status");
            }
            ProofOfDelivery proof = ProofOfDelivery.builder()
                    .shipment(shipment)
                    .deliveryAssignment(assignment)
                    .recipientNameSigned(request.getProofData())
                    .deliveryNote(request.getNote())
                    .build();
            proofOfDeliveryRepository.save(proof);

            // Update assignment status
            assignment.setStatus(AssignmentStatus.COMPLETED);
            assignmentRepository.save(assignment);

            // Update vehicle status
            Vehicle vehicle = assignment.getVehicle();
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);

        } else if (request.getStatus() == ShipmentStatus.FAILED_DELIVERY) {
            if (request.getFailureReason() == null) {
                throw new BadRequestException("Failure reason is required for FAILED_DELIVERY status");
            }

            List<FailedDeliveryAttempt> attempts = failedDeliveryAttemptRepository.findByShipmentIdOrderByAttemptNumberDesc(shipmentId);
            int nextAttemptNumber = attempts.isEmpty() ? 1 : attempts.get(0).getAttemptNumber() + 1;

            FailedDeliveryAttempt attempt = FailedDeliveryAttempt.builder()
                    .shipment(shipment)
                    .reason(request.getFailureReason())
                    .attemptNumber(nextAttemptNumber)
                    .driver(driver)
                    .build();
            failedDeliveryAttemptRepository.save(attempt);
        }
    }
}

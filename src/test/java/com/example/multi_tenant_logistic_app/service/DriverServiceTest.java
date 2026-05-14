package com.example.multi_tenant_logistic_app.service;

import com.example.multi_tenant_logistic_app.domain.entity.*;
import com.example.multi_tenant_logistic_app.domain.enumeration.AssignmentStatus;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus;
import com.example.multi_tenant_logistic_app.domain.enumeration.VehicleStatus;
import com.example.multi_tenant_logistic_app.dto.requests.DriverStatusUpdateRequest;
import com.example.multi_tenant_logistic_app.repository.*;
import com.example.multi_tenant_logistic_app.service.impl.DriverServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DriverServiceTest {

    @Mock
    private DeliveryAssignmentRepository assignmentRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentStatusUpdateRepository statusUpdateRepository;

    @Mock
    private ProofOfDeliveryRepository proofOfDeliveryRepository;

    @Mock
    private FailedDeliveryAttemptRepository failedDeliveryAttemptRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private DriverServiceImpl driverService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetActiveAssignment_Success() {
        Long driverId = 1L;
        DeliveryAssignment assignment = new DeliveryAssignment();
        List<DeliveryAssignment> list = new ArrayList<>();
        list.add(assignment);

        when(assignmentRepository.findByDriverIdAndStatus(driverId, AssignmentStatus.ACTIVE)).thenReturn(list);

        Optional<DeliveryAssignment> result = driverService.getActiveAssignment(driverId);

        assertTrue(result.isPresent());
        assertEquals(assignment, result.get());
    }

    @Test
    public void testUpdateShipmentStatus_InTransit() {
        Long driverId = 1L;
        Long shipmentId = 100L;

        DriverStatusUpdateRequest request = new DriverStatusUpdateRequest();
        request.setStatus(ShipmentStatus.IN_TRANSIT);
        request.setNote("On the way");

        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);

        User driver = new User();
        driver.setId(driverId);

        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setShipment(shipment);
        assignment.setDriver(driver);

        List<DeliveryAssignment> list = new ArrayList<>();
        list.add(assignment);

        when(assignmentRepository.findByDriverIdAndStatus(driverId, AssignmentStatus.ACTIVE)).thenReturn(list);
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));

        driverService.updateShipmentStatus(driverId, shipmentId, request);

        assertEquals(ShipmentStatus.IN_TRANSIT, shipment.getStatus());
        verify(statusUpdateRepository, times(1)).save(any(ShipmentStatusUpdate.class));
    }

    @Test
    public void testUpdateShipmentStatus_Delivered() {
        Long driverId = 1L;
        Long shipmentId = 100L;

        DriverStatusUpdateRequest request = new DriverStatusUpdateRequest();
        request.setStatus(ShipmentStatus.DELIVERED);
        request.setProofData("signature_data");
        request.setProofType("SIGNATURE");

        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);

        User driver = new User();
        driver.setId(driverId);

        Vehicle vehicle = new Vehicle();
        vehicle.setStatus(VehicleStatus.ON_DELIVERY);

        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setShipment(shipment);
        assignment.setDriver(driver);
        assignment.setVehicle(vehicle);

        List<DeliveryAssignment> list = new ArrayList<>();
        list.add(assignment);

        when(assignmentRepository.findByDriverIdAndStatus(driverId, AssignmentStatus.ACTIVE)).thenReturn(list);
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));

        driverService.updateShipmentStatus(driverId, shipmentId, request);

        assertEquals(ShipmentStatus.DELIVERED, shipment.getStatus());
        assertEquals(AssignmentStatus.COMPLETED, assignment.getStatus());
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
        verify(proofOfDeliveryRepository, times(1)).save(any(ProofOfDelivery.class));
    }
}

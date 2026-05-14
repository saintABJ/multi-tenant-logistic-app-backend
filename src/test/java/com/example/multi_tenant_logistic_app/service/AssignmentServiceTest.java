package com.example.multi_tenant_logistic_app.service;

import com.example.multi_tenant_logistic_app.domain.entity.*;
import com.example.multi_tenant_logistic_app.domain.enumeration.AssignmentStatus;
import com.example.multi_tenant_logistic_app.domain.enumeration.VehicleStatus;
import com.example.multi_tenant_logistic_app.dto.requests.AssignShipmentRequest;
import com.example.multi_tenant_logistic_app.repository.*;
import com.example.multi_tenant_logistic_app.service.impl.AssignmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AssignmentServiceTest {

    @Mock
    private DeliveryAssignmentRepository assignmentRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverProfileRepository driverProfileRepository;

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAssignShipment_Success() {
        Long dispatcherId = 1L;
        AssignShipmentRequest request = new AssignShipmentRequest();
        request.setShipmentId(100L);
        request.setDriverId(2L);
        request.setVehicleId(3L);

        Shipment shipment = new Shipment();
        shipment.setId(100L);

        User driver = new User();
        driver.setId(2L);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(3L);
        vehicle.setStatus(VehicleStatus.AVAILABLE);

        User dispatcher = new User();
        dispatcher.setId(dispatcherId);

        when(shipmentRepository.findById(100L)).thenReturn(Optional.of(shipment));
        when(userRepository.findById(2L)).thenReturn(Optional.of(driver));
        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(vehicle));
        when(userRepository.findById(dispatcherId)).thenReturn(Optional.of(dispatcher));
        when(assignmentRepository.save(any(DeliveryAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryAssignment assignment = assignmentService.assignShipment(dispatcherId, request);

        assertNotNull(assignment);
        assertEquals(AssignmentStatus.ACTIVE, assignment.getStatus());
        assertEquals(VehicleStatus.ON_DELIVERY, vehicle.getStatus());
        verify(assignmentRepository, times(1)).save(any(DeliveryAssignment.class));
    }

    @Test
    public void testAssignShipment_VehicleNotAvailable() {
        Long dispatcherId = 1L;
        AssignShipmentRequest request = new AssignShipmentRequest();
        request.setShipmentId(100L);
        request.setDriverId(2L);
        request.setVehicleId(3L);

        Shipment shipment = new Shipment();
        Vehicle vehicle = new Vehicle();
        vehicle.setStatus(VehicleStatus.ON_DELIVERY);

        when(shipmentRepository.findById(100L)).thenReturn(Optional.of(shipment));
        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(vehicle));

        assertThrows(RuntimeException.class, () -> assignmentService.assignShipment(dispatcherId, request));
    }
}

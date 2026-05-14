package com.example.multi_tenant_logistic_app.service;

import com.example.multi_tenant_logistic_app.domain.entity.DeliveryAssignment;
import com.example.multi_tenant_logistic_app.domain.entity.Shipment;
import com.example.multi_tenant_logistic_app.domain.entity.ShipmentStatusUpdate;
import com.example.multi_tenant_logistic_app.domain.entity.User;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus;
import com.example.multi_tenant_logistic_app.dto.requests.AssignShipmentRequest;
import com.example.multi_tenant_logistic_app.repository.ShipmentRepository;
import com.example.multi_tenant_logistic_app.repository.ShipmentStatusUpdateRepository;
import com.example.multi_tenant_logistic_app.repository.UserRepository;
import com.example.multi_tenant_logistic_app.service.impl.DispatcherServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DispatcherServiceTest {

    @Mock
    private AssignmentService assignmentService;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentStatusUpdateRepository statusUpdateRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DispatcherServiceImpl dispatcherService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAssignShipment() {
        Long dispatcherId = 1L;
        AssignShipmentRequest request = new AssignShipmentRequest();
        DeliveryAssignment assignment = new DeliveryAssignment();

        when(assignmentService.assignShipment(dispatcherId, request)).thenReturn(assignment);

        DeliveryAssignment result = dispatcherService.assignShipment(dispatcherId, request);

        assertEquals(assignment, result);
    }

    @Test
    public void testReassignShipment() {
        Long dispatcherId = 1L;
        Long currentAssignmentId = 2L;
        AssignShipmentRequest request = new AssignShipmentRequest();
        DeliveryAssignment assignment = new DeliveryAssignment();

        when(assignmentService.reassignShipment(dispatcherId, currentAssignmentId, request)).thenReturn(assignment);

        DeliveryAssignment result = dispatcherService.reassignShipment(dispatcherId, currentAssignmentId, request);

        assertEquals(assignment, result);
    }

    @Test
    public void testUpdateShipmentStatus_Success() {
        Long dispatcherId = 1L;
        Long shipmentId = 100L;
        ShipmentStatus status = ShipmentStatus.PICKED_UP;
        String note = "Picked up";
        String city = "Lagos";
        String state = "Lagos";

        Shipment shipment = new Shipment();
        shipment.setId(shipmentId);

        User dispatcher = new User();
        dispatcher.setId(dispatcherId);

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(userRepository.findById(dispatcherId)).thenReturn(Optional.of(dispatcher));

        dispatcherService.updateShipmentStatus(dispatcherId, shipmentId, status, note, city, state);

        assertEquals(status, shipment.getStatus());
        verify(shipmentRepository, times(1)).save(shipment);
        verify(statusUpdateRepository, times(1)).save(any(ShipmentStatusUpdate.class));
    }

    @Test
    public void testUpdateShipmentStatus_InvalidStatus() {
        Long dispatcherId = 1L;
        Long shipmentId = 100L;
        ShipmentStatus status = ShipmentStatus.DELIVERED; // Invalid for dispatcher

        assertThrows(RuntimeException.class, () -> 
                dispatcherService.updateShipmentStatus(dispatcherId, shipmentId, status, null, null, null));
    }

    @Test
    public void testUpdateShipmentStatus_ShipmentNotFound() {
        Long dispatcherId = 1L;
        Long shipmentId = 100L;
        ShipmentStatus status = ShipmentStatus.PICKED_UP;

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
                dispatcherService.updateShipmentStatus(dispatcherId, shipmentId, status, null, null, null));
    }
}

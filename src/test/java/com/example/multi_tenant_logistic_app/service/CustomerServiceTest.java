package com.example.multi_tenant_logistic_app.service;

import com.example.multi_tenant_logistic_app.domain.entity.*;
import com.example.multi_tenant_logistic_app.domain.enumeration.AssignmentStatus;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus;
import com.example.multi_tenant_logistic_app.dto.requests.BookShipmentRequest;
import com.example.multi_tenant_logistic_app.dto.requests.RateDeliveryRequest;
import com.example.multi_tenant_logistic_app.repository.*;
import com.example.multi_tenant_logistic_app.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CustomerServiceTest {

    @Mock
    private ShipmentService shipmentService;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private DeliveryRatingRepository ratingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeliveryAssignmentRepository assignmentRepository;

    @Mock
    private DriverProfileRepository driverProfileRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testBookShipment() {
        Long customerId = 1L;
        Long companyId = 2L;
        BookShipmentRequest request = new BookShipmentRequest();
        Shipment shipment = new Shipment();

        when(shipmentService.bookShipment(customerId, companyId, request)).thenReturn(shipment);

        Shipment result = customerService.bookShipment(customerId, companyId, request);

        assertEquals(shipment, result);
    }

    @Test
    public void testGetShipmentHistory() {
        Long customerId = 1L;
        List<Shipment> list = new ArrayList<>();

        when(shipmentService.getCustomerShipments(customerId)).thenReturn(list);

        List<Shipment> result = customerService.getShipmentHistory(customerId);

        assertEquals(list, result);
    }

    @Test
    public void testRateDelivery_Success() {
        Long customerId = 1L;
        RateDeliveryRequest request = new RateDeliveryRequest();
        request.setShipmentId(100L);
        request.setRating(5);
        request.setReviewText("Great");

        User customer = new User();
        customer.setId(customerId);

        Shipment shipment = new Shipment();
        shipment.setId(100L);
        shipment.setCustomer(customer);
        shipment.setStatus(ShipmentStatus.DELIVERED);

        User driver = new User();
        driver.setId(2L);

        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setDriver(driver);
        assignment.setStatus(AssignmentStatus.COMPLETED);

        DriverProfile profile = new DriverProfile();
        profile.setTotalDeliveries(10);
        profile.setRatingAverage(BigDecimal.valueOf(4.5));

        when(shipmentRepository.findById(100L)).thenReturn(Optional.of(shipment));
        when(ratingRepository.findByShipmentId(100L)).thenReturn(Optional.empty());
        when(assignmentRepository.findByShipmentIdAndStatus(100L, AssignmentStatus.COMPLETED)).thenReturn(Optional.of(assignment));
        when(ratingRepository.getAverageRatingForDriver(2L)).thenReturn(5.0);
        when(driverProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));

        customerService.rateDelivery(customerId, request);

        verify(ratingRepository, times(1)).save(any(DeliveryRating.class));
        assertEquals(BigDecimal.valueOf(5.0), profile.getRatingAverage());
    }
}

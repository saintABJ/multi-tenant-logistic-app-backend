package com.example.multi_tenant_logistic_app.service;

import com.example.multi_tenant_logistic_app.domain.entity.*;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentType;
import com.example.multi_tenant_logistic_app.dto.requests.BookShipmentRequest;
import com.example.multi_tenant_logistic_app.dto.responses.ShipmentTrackingResponse;
import com.example.multi_tenant_logistic_app.repository.*;
import com.example.multi_tenant_logistic_app.service.impl.ShipmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LogisticsCompanyRepository companyRepository;

    @Mock
    private PricingService pricingService;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ShipmentStatusUpdateRepository statusUpdateRepository;

    @InjectMocks
    private ShipmentServiceImpl shipmentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testBookShipment_Success() {
        Long customerId = 1L;
        Long companyId = 2L;

        BookShipmentRequest request = new BookShipmentRequest();
        request.setSenderName("Sender");
        request.setSenderPhone("123");
        request.setSenderAddress("Addr1");
        request.setRecipientName("Recipient");
        request.setRecipientPhone("456");
        request.setRecipientAddress("Addr2");
        request.setRecipientCity("City");
        request.setRecipientState("State");
        request.setPackageDescription("Box");
        request.setWeightKg(BigDecimal.valueOf(10.0));
        request.setShipmentType(ShipmentType.EXPRESS);

        User customer = new User();
        customer.setId(customerId);

        LogisticsCompany company = new LogisticsCompany();
        company.setId(companyId);

        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(pricingService.computeFee(eq(companyId), any(ShipmentType.class), any(BigDecimal.class), any()))
                .thenReturn(BigDecimal.valueOf(50.0));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
            Shipment s = invocation.getArgument(0);
            s.setId(100L);
            s.setTrackingNumber("TRK123");
            return s;
        });
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Shipment shipment = shipmentService.bookShipment(customerId, companyId, request);

        assertNotNull(shipment);
        assertEquals("TRK123", shipment.getTrackingNumber());
        assertEquals(ShipmentStatus.PENDING, shipment.getStatus());
        assertEquals(BigDecimal.valueOf(50.0), shipment.getTotalFee());
        verify(shipmentRepository, times(1)).save(any(Shipment.class));
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    public void testTrackShipment_Success() {
        String trackingNumber = "TRK123";
        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(trackingNumber);
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);

        List<ShipmentStatusUpdate> history = new ArrayList<>();
        ShipmentStatusUpdate update = new ShipmentStatusUpdate();
        update.setStatus(ShipmentStatus.PENDING);
        update.setUpdatedAt(LocalDateTime.now().minusHours(1));
        history.add(update);

        when(shipmentRepository.findByTrackingNumber(trackingNumber)).thenReturn(Optional.of(shipment));
        when(statusUpdateRepository.findByShipmentIdOrderByUpdatedAtDesc(shipment.getId())).thenReturn(history);

        ShipmentTrackingResponse response = shipmentService.trackShipment(trackingNumber);

        assertNotNull(response);
        assertEquals(trackingNumber, response.getTrackingNumber());
        assertEquals(ShipmentStatus.IN_TRANSIT, response.getCurrentStatus());
        assertFalse(response.getHistory().isEmpty());
    }

    @Test
    public void testTrackShipment_NotFound() {
        String trackingNumber = "TRK123";
        when(shipmentRepository.findByTrackingNumber(trackingNumber)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> shipmentService.trackShipment(trackingNumber));
    }
}

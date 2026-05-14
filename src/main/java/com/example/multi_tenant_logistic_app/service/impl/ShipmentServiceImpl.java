package com.example.multi_tenant_logistic_app.service.impl;

import com.example.multi_tenant_logistic_app.domain.entity.*;
import com.example.multi_tenant_logistic_app.domain.enumeration.InvoiceStatus;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus;
import com.example.multi_tenant_logistic_app.dto.requests.BookShipmentRequest;
import com.example.multi_tenant_logistic_app.dto.responses.ShipmentTrackingResponse;
import com.example.multi_tenant_logistic_app.repository.*;
import com.example.multi_tenant_logistic_app.service.PricingService;
import com.example.multi_tenant_logistic_app.service.ShipmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final PricingService pricingService;
    private final UserRepository userRepository;
    private final LogisticsCompanyRepository companyRepository;
    private final InvoiceRepository invoiceRepository;
    private final ShipmentStatusUpdateRepository statusUpdateRepository;
    private final WarehouseRepository warehouseRepository;

    public ShipmentServiceImpl(ShipmentRepository shipmentRepository,
                               PricingService pricingService,
                               UserRepository userRepository,
                               LogisticsCompanyRepository companyRepository,
                               InvoiceRepository invoiceRepository,
                               ShipmentStatusUpdateRepository statusUpdateRepository,
                               WarehouseRepository warehouseRepository) {
        this.shipmentRepository = shipmentRepository;
        this.pricingService = pricingService;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.invoiceRepository = invoiceRepository;
        this.statusUpdateRepository = statusUpdateRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    @Transactional
    public Shipment bookShipment(Long customerId, Long companyId, BookShipmentRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        LogisticsCompany company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Warehouse warehouse = null;
        if (request.getOriginWarehouseId() != null) {
            warehouse = warehouseRepository.findById(request.getOriginWarehouseId())
                    .orElseThrow(() -> new RuntimeException("Warehouse not found"));
        }

        BigDecimal fee = pricingService.computeFee(companyId, request.getShipmentType(), request.getWeightKg(), request.getDeclaredValue());

        String trackingNumber = "LGS-" + companyId + "-" + UUID.randomUUID().toString().substring(0, 8);

        Shipment shipment = Shipment.builder()
                .company(company)
                .customer(customer)
                .originWarehouse(warehouse)
                .trackingNumber(trackingNumber)
                .senderName(request.getSenderName())
                .senderPhone(request.getSenderPhone())
                .senderAddress(request.getSenderAddress())
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .recipientAddress(request.getRecipientAddress())
                .recipientCity(request.getRecipientCity())
                .recipientState(request.getRecipientState())
                .packageDescription(request.getPackageDescription())
                .weightKg(request.getWeightKg())
                .declaredValue(request.getDeclaredValue())
                .shipmentType(request.getShipmentType())
                .status(ShipmentStatus.PENDING)
                .baseFee(fee)
                .totalFee(fee)
                .build();

        shipment = shipmentRepository.save(shipment);

        Invoice invoice = Invoice.builder()
                .shipment(shipment)
                .customer(customer)
                .company(company)
                .amount(fee)
                .status(InvoiceStatus.UNPAID)
                .build();

        invoiceRepository.save(invoice);


        ShipmentStatusUpdate statusUpdate = ShipmentStatusUpdate.builder()
                .shipment(shipment)
                .status(ShipmentStatus.PENDING)
                .note("Shipment booked")
                .updatedBy(customer) // Booked by customer
                .build();

        statusUpdateRepository.save(statusUpdate);

        return shipment;
    }

    @Override
    public ShipmentTrackingResponse trackShipment(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        List<ShipmentStatusUpdate> updates = statusUpdateRepository.findByShipmentIdOrderByUpdatedAtDesc(shipment.getId());

        List<ShipmentTrackingResponse.StatusUpdateDto> history = updates.stream()
                .map(u -> ShipmentTrackingResponse.StatusUpdateDto.builder()
                        .status(u.getStatus())
                        .note(u.getNote())
                        .location(u.getLocationCity() != null ? u.getLocationCity() + ", " + u.getLocationState() : "N/A")
                        .updatedAt(u.getUpdatedAt().toString())
                        .build())
                .collect(Collectors.toList());

        return ShipmentTrackingResponse.builder()
                .trackingNumber(shipment.getTrackingNumber())
                .currentStatus(shipment.getStatus())
                .history(history)
                .build();
    }

    @Override
    public List<Shipment> getCustomerShipments(Long customerId) {
        return shipmentRepository.findByCustomerId(customerId);
    }
}

package com.example.multi_tenant_logistic_app.controller;

import com.example.multi_tenant_logistic_app.domain.entity.Shipment;
import com.example.multi_tenant_logistic_app.domain.entity.User;
import com.example.multi_tenant_logistic_app.dto.requests.BookShipmentRequest;
import com.example.multi_tenant_logistic_app.dto.requests.RateDeliveryRequest;
import com.example.multi_tenant_logistic_app.dto.responses.ShipmentTrackingResponse;
import com.example.multi_tenant_logistic_app.repository.UserRepository;
import com.example.multi_tenant_logistic_app.service.CustomerService;
import com.example.multi_tenant_logistic_app.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CustomerController {

    private final CustomerService customerService;
    private final ShipmentService shipmentService;
    private final UserRepository userRepository;

    public CustomerController(CustomerService customerService, ShipmentService shipmentService, UserRepository userRepository) {
        this.customerService = customerService;
        this.shipmentService = shipmentService;
        this.userRepository = userRepository;
    }

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    // Protected Customer endpoints
    @PostMapping("/customer/shipments")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Shipment> bookShipment(@RequestParam Long companyId, @Valid @RequestBody BookShipmentRequest request) {
        return ResponseEntity.ok(customerService.bookShipment(getCurrentUserId(), companyId, request));
    }

    @GetMapping("/customer/shipments/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<Shipment>> getShipmentHistory() {
        return ResponseEntity.ok(customerService.getShipmentHistory(getCurrentUserId()));
    }

    @PostMapping("/customer/shipments/rate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> rateDelivery(@Valid @RequestBody RateDeliveryRequest request) {
        customerService.rateDelivery(getCurrentUserId(), request);
        return ResponseEntity.ok().build();
    }

    // Public endpoint
    @GetMapping("/shipments/track/{trackingNumber}")
    public ResponseEntity<ShipmentTrackingResponse> trackShipment(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(shipmentService.trackShipment(trackingNumber));
    }
}

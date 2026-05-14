package com.example.multi_tenant_logistic_app.controller;

import com.example.multi_tenant_logistic_app.domain.entity.DeliveryAssignment;
import com.example.multi_tenant_logistic_app.domain.entity.User;
import com.example.multi_tenant_logistic_app.dto.requests.DriverStatusUpdateRequest;
import com.example.multi_tenant_logistic_app.repository.UserRepository;
import com.example.multi_tenant_logistic_app.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/driver")
@PreAuthorize("hasRole('DRIVER')")
public class DriverController {

    private final DriverService driverService;
    private final UserRepository userRepository;

    public DriverController(DriverService driverService, UserRepository userRepository) {
        this.driverService = driverService;
        this.userRepository = userRepository;
    }

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping("/active-assignment")
    public ResponseEntity<DeliveryAssignment> getActiveAssignment() {
        Optional<DeliveryAssignment> assignment = driverService.getActiveAssignment(getCurrentUserId());
        return assignment.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/shipments/{id}/status")
    public ResponseEntity<Void> updateShipmentStatus(@PathVariable Long id, @Valid @RequestBody DriverStatusUpdateRequest request) {
        driverService.updateShipmentStatus(getCurrentUserId(), id, request);
        return ResponseEntity.ok().build();
    }
}

package com.example.multi_tenant_logistic_app.controller;

import com.example.multi_tenant_logistic_app.domain.entity.DeliveryAssignment;
import com.example.multi_tenant_logistic_app.domain.entity.User;
import com.example.multi_tenant_logistic_app.dto.requests.AssignShipmentRequest;
import com.example.multi_tenant_logistic_app.dto.requests.StatusUpdateRequest;
import com.example.multi_tenant_logistic_app.repository.UserRepository;
import com.example.multi_tenant_logistic_app.service.DispatcherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dispatcher")
@PreAuthorize("hasRole('DISPATCHER')")
public class DispatcherController {

    private final DispatcherService dispatcherService;
    private final UserRepository userRepository;

    public DispatcherController(DispatcherService dispatcherService, UserRepository userRepository) {
        this.dispatcherService = dispatcherService;
        this.userRepository = userRepository;
    }

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @PostMapping("/assignments")
    public ResponseEntity<DeliveryAssignment> assignShipment(@Valid @RequestBody AssignShipmentRequest request) {
        return ResponseEntity.ok(dispatcherService.assignShipment(getCurrentUserId(), request));
    }

    @PostMapping("/assignments/{id}/reassign")
    public ResponseEntity<DeliveryAssignment> reassignShipment(@PathVariable Long id, @Valid @RequestBody AssignShipmentRequest request) {
        return ResponseEntity.ok(dispatcherService.reassignShipment(getCurrentUserId(), id, request));
    }

    @PostMapping("/shipments/{id}/status")
    public ResponseEntity<Void> updateShipmentStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        dispatcherService.updateShipmentStatus(getCurrentUserId(), id, request.getStatus(), request.getNote(), request.getLocationCity(), request.getLocationState());
        return ResponseEntity.ok().build();
    }
}

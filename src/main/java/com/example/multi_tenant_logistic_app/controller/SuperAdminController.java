package com.example.multi_tenant_logistic_app.controller;

import com.example.multi_tenant_logistic_app.dto.responses.CompanyStatsResponse;
import com.example.multi_tenant_logistic_app.dto.responses.PlatformStatsResponse;
import com.example.multi_tenant_logistic_app.service.SuperAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/super-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    public SuperAdminController(SuperAdminService superAdminService) {
        this.superAdminService = superAdminService;
    }

    @PostMapping("/companies/{id}/approve")
    public ResponseEntity<String> approveCompany(@PathVariable Long id) {
        superAdminService.approveCompany(id);
        return ResponseEntity.ok("Company approved successfully");
    }

    @PostMapping("/companies/{id}/suspend")
    public ResponseEntity<String> suspendCompany(@PathVariable Long id) {
        superAdminService.suspendCompany(id);
        return ResponseEntity.ok("Company suspended successfully");
    }

    @GetMapping("/companies/stats")
    public ResponseEntity<List<CompanyStatsResponse>> getCompaniesWithStats() {
        return ResponseEntity.ok(superAdminService.getCompaniesWithStats());
    }

    @GetMapping("/platform/stats")
    public ResponseEntity<PlatformStatsResponse> getPlatformStats() {
        return ResponseEntity.ok(superAdminService.getPlatformStats());
    }
}

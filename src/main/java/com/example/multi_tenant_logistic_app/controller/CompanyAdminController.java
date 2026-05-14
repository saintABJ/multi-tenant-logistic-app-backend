package com.example.multi_tenant_logistic_app.controller;

import com.example.multi_tenant_logistic_app.domain.entity.Warehouse;
import com.example.multi_tenant_logistic_app.domain.entity.Vehicle;
import com.example.multi_tenant_logistic_app.domain.entity.PricingConfig;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus;
import com.example.multi_tenant_logistic_app.dto.responses.DriverResponse;
import com.example.multi_tenant_logistic_app.dto.responses.RevenueReportResponse;
import com.example.multi_tenant_logistic_app.domain.entity.Shipment;
import com.example.multi_tenant_logistic_app.dto.requests.CreateDriverRequest;
import com.example.multi_tenant_logistic_app.security.TenantContext;
import com.example.multi_tenant_logistic_app.service.CompanyAdminService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/company-admin")
@PreAuthorize("hasRole('COMPANY_ADMIN')")
public class CompanyAdminController {

    private final CompanyAdminService companyAdminService;

    public CompanyAdminController(CompanyAdminService companyAdminService) {
        this.companyAdminService = companyAdminService;
    }

    private Long getCompanyId() {
        return TenantContext.getCurrentTenant();
    }

    // Warehouses
    @GetMapping("/warehouses")
    public ResponseEntity<List<Warehouse>> getWarehouses() {
        return ResponseEntity.ok(companyAdminService.getWarehouses(getCompanyId()));
    }

    @PostMapping("/warehouse/create")
    public ResponseEntity<Warehouse> createWarehouse(@Valid @RequestBody Warehouse warehouse) {
        return ResponseEntity.ok(companyAdminService.createWarehouse(getCompanyId(), warehouse));
    }

    @PutMapping("/warehouse/update/{id}")
    public ResponseEntity<Warehouse> updateWarehouse(@PathVariable Long id, @Valid @RequestBody Warehouse warehouse) {
        return ResponseEntity.ok(companyAdminService.updateWarehouse(getCompanyId(), id, warehouse));
    }

    @DeleteMapping("/warehouse/delete/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        companyAdminService.deleteWarehouse(getCompanyId(), id);
        return ResponseEntity.noContent().build();
    }

    // Vehicles
    @GetMapping("/vehicles")
    public ResponseEntity<List<Vehicle>> getVehicles() {
        return ResponseEntity.ok(companyAdminService.getVehicles(getCompanyId()));
    }

    @PostMapping("/vehicle/create")
    public ResponseEntity<Vehicle> createVehicle(@Valid @RequestBody Vehicle vehicle) {
        return ResponseEntity.ok(companyAdminService.createVehicle(getCompanyId(), vehicle));
    }

    @PutMapping("/vehicles/update/{id}")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable Long id, @Valid @RequestBody Vehicle vehicle) {
        return ResponseEntity.ok(companyAdminService.updateVehicle(getCompanyId(), id, vehicle));
    }

    @DeleteMapping("/vehicles/delete/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        companyAdminService.deleteVehicle(getCompanyId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/vehicle/{id}/assign-driver")
    public ResponseEntity<Void> assignDriverToVehicle(@PathVariable Long id, @RequestParam Long driverId) {
        companyAdminService.assignDriverToVehicle(getCompanyId(), id, driverId);
        return ResponseEntity.ok().build();
    }

    // Shipments
    @GetMapping("/shipments")
    public ResponseEntity<List<Shipment>> getShipments(
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(companyAdminService.getShipments(getCompanyId(), status, start, end));
    }

    // Drivers
    @PostMapping("/driver/create")
    public ResponseEntity<DriverResponse> createDriver(@Valid @RequestBody CreateDriverRequest request) {
        return ResponseEntity.ok(companyAdminService.createDriver(getCompanyId(), request));
    }

    @GetMapping("/drivers")
    public ResponseEntity<List<DriverResponse>> getDrivers() {
        return ResponseEntity.ok(companyAdminService.getDrivers(getCompanyId()));
    }

    // Reports
    @GetMapping("/reports/revenue")
    public ResponseEntity<List<RevenueReportResponse>> getRevenueReport() {
        return ResponseEntity.ok(companyAdminService.getRevenueReport(getCompanyId()));
    }

    @GetMapping("/reports/driver-performance")
    public ResponseEntity<List<com.example.multi_tenant_logistic_app.repository.projection.DriverPerformanceProjection>> getDriverPerformanceReport() {
        return ResponseEntity.ok(companyAdminService.getDriverPerformanceReport(getCompanyId()));
    }

    @GetMapping("/reports/heat-map")
    public ResponseEntity<List<com.example.multi_tenant_logistic_app.repository.projection.HeatMapProjection>> getHeatMap() {
        return ResponseEntity.ok(companyAdminService.getHeatMap(getCompanyId()));
    }

    // Pricing
    @PostMapping("/pricing")
    public ResponseEntity<PricingConfig> setPricingConfig(@Valid @RequestBody PricingConfig config) {
        return ResponseEntity.ok(companyAdminService.setPricingConfig(getCompanyId(), config));
    }
}

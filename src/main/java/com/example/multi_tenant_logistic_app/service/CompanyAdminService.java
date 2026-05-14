package com.example.multi_tenant_logistic_app.service;

import com.example.multi_tenant_logistic_app.domain.entity.Warehouse;
import com.example.multi_tenant_logistic_app.domain.entity.Vehicle;
import com.example.multi_tenant_logistic_app.domain.entity.PricingConfig;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus;
import com.example.multi_tenant_logistic_app.dto.responses.DriverResponse;
import com.example.multi_tenant_logistic_app.dto.responses.RevenueReportResponse;
import com.example.multi_tenant_logistic_app.dto.requests.CreateDriverRequest;
import com.example.multi_tenant_logistic_app.domain.entity.Shipment;
import java.time.LocalDateTime;
import java.util.List;

public interface CompanyAdminService {
    // Warehouse CRUD
    Warehouse createWarehouse(Long companyId, Warehouse warehouse);
    Warehouse updateWarehouse(Long companyId, Long id, Warehouse warehouse);
    void deleteWarehouse(Long companyId, Long id);
    List<Warehouse> getWarehouses(Long companyId);

    // Vehicle CRUD
    Vehicle createVehicle(Long companyId, Vehicle vehicle);
    Vehicle updateVehicle(Long companyId, Long id, Vehicle vehicle);
    void deleteVehicle(Long companyId, Long id);
    List<Vehicle> getVehicles(Long companyId);
    void assignDriverToVehicle(Long companyId, Long vehicleId, Long driverId);

    // Shipments
    List<Shipment> getShipments(Long companyId, ShipmentStatus status, LocalDateTime start, LocalDateTime end);

    // Drivers
    DriverResponse createDriver(Long companyId, CreateDriverRequest request);
    List<DriverResponse> getDrivers(Long companyId);

    // Reports
    List<RevenueReportResponse> getRevenueReport(Long companyId);
    List<com.example.multi_tenant_logistic_app.repository.projection.DriverPerformanceProjection> getDriverPerformanceReport(Long companyId);
    List<com.example.multi_tenant_logistic_app.repository.projection.HeatMapProjection> getHeatMap(Long companyId);

    // Pricing
    PricingConfig setPricingConfig(Long companyId, PricingConfig config);
}

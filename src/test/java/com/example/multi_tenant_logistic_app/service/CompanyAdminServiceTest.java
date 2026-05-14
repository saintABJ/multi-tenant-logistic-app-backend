package com.example.multi_tenant_logistic_app.service;

import com.example.multi_tenant_logistic_app.domain.entity.*;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus;
import com.example.multi_tenant_logistic_app.dto.responses.DriverResponse;
import com.example.multi_tenant_logistic_app.dto.responses.RevenueReportResponse;
import com.example.multi_tenant_logistic_app.repository.*;
import com.example.multi_tenant_logistic_app.repository.projection.RevenueReportProjection;
import com.example.multi_tenant_logistic_app.service.impl.CompanyAdminServiceImpl;
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
import static org.mockito.Mockito.*;

public class CompanyAdminServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private DriverProfileRepository driverProfileRepository;
    @Mock
    private PricingConfigRepository pricingConfigRepository;
    @Mock
    private LogisticsCompanyRepository companyRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CompanyAdminServiceImpl companyAdminService;

    private LogisticsCompany company;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        company = new LogisticsCompany();
        company.setId(1L);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
    }

    // Warehouse Tests
    @Test
    public void testCreateWarehouse() {
        Warehouse warehouse = new Warehouse();
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        Warehouse result = companyAdminService.createWarehouse(1L, warehouse);

        assertNotNull(result);
        assertEquals(company, warehouse.getCompany());
        verify(warehouseRepository, times(1)).save(warehouse);
    }

    @Test
    public void testUpdateWarehouse_Success() {
        Warehouse existing = new Warehouse();
        existing.setCompany(company);
        Warehouse details = new Warehouse();
        details.setName("New Name");

        when(warehouseRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(existing);

        Warehouse result = companyAdminService.updateWarehouse(1L, 100L, details);

        assertEquals("New Name", existing.getName());
        verify(warehouseRepository, times(1)).save(existing);
    }

    @Test
    public void testDeleteWarehouse_Success() {
        Warehouse existing = new Warehouse();
        existing.setCompany(company);

        when(warehouseRepository.findById(100L)).thenReturn(Optional.of(existing));

        companyAdminService.deleteWarehouse(1L, 100L);

        verify(warehouseRepository, times(1)).delete(existing);
    }

    // Vehicle Tests
    @Test
    public void testCreateVehicle() {
        Vehicle vehicle = new Vehicle();
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        Vehicle result = companyAdminService.createVehicle(1L, vehicle);

        assertNotNull(result);
        assertEquals(company, vehicle.getCompany());
        verify(vehicleRepository, times(1)).save(vehicle);
    }

    @Test
    public void testUpdateVehicle_Success() {
        Vehicle existing = new Vehicle();
        existing.setCompany(company);
        Vehicle details = new Vehicle();
        details.setPlateNumber("XYZ");

        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(existing);

        Vehicle result = companyAdminService.updateVehicle(1L, 100L, details);

        assertEquals("XYZ", existing.getPlateNumber());
        verify(vehicleRepository, times(1)).save(existing);
    }

    // Assign Driver
    @Test
    public void testAssignDriverToVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setCompany(company);
        User driver = new User();
        driver.setCompany(company);
        DriverProfile profile = new DriverProfile();

        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(vehicle));
        when(userRepository.findById(2L)).thenReturn(Optional.of(driver));
        when(driverProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));

        companyAdminService.assignDriverToVehicle(1L, 100L, 2L);

        assertEquals(driver, vehicle.getDriver());
        assertEquals(vehicle, profile.getAssignedVehicle());
        verify(vehicleRepository, times(1)).save(vehicle);
        verify(driverProfileRepository, times(1)).save(profile);
    }

    // Reports
    @Test
    public void testGetRevenueReport() {
        List<RevenueReportProjection> projections = new ArrayList<>();
        // Projections are interfaces, so we need to mock them or use a concrete class if available.
        // Let's assume we can mock them.
        RevenueReportProjection p = mock(RevenueReportProjection.class);
        when(p.getShipmentType()).thenReturn("EXPRESS");
        when(p.getMonth()).thenReturn(5);
        when(p.getTotalFees()).thenReturn(BigDecimal.valueOf(1000));
        projections.add(p);

        when(shipmentRepository.getRevenueReport(1L)).thenReturn(projections);

        List<RevenueReportResponse> result = companyAdminService.getRevenueReport(1L);

        assertEquals(1, result.size());
        assertEquals("EXPRESS", result.get(0).getShipmentType());
        assertEquals(BigDecimal.valueOf(1000), result.get(0).getTotalFees());
    }
}

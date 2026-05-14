package com.example.multi_tenant_logistic_app.service.impl;

import com.example.multi_tenant_logistic_app.domain.entity.*;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus;
import com.example.multi_tenant_logistic_app.dto.responses.DriverResponse;
import com.example.multi_tenant_logistic_app.dto.responses.RevenueReportResponse;
import com.example.multi_tenant_logistic_app.exception.ResourceNotFoundException;
import com.example.multi_tenant_logistic_app.exception.UnauthorizedAccessException;
import com.example.multi_tenant_logistic_app.repository.*;
import com.example.multi_tenant_logistic_app.repository.projection.RevenueReportProjection;
import com.example.multi_tenant_logistic_app.service.CompanyAdminService;
import com.example.multi_tenant_logistic_app.domain.enumeration.AccountType;
import com.example.multi_tenant_logistic_app.dto.requests.CreateDriverRequest;
import com.example.multi_tenant_logistic_app.exception.UserAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyAdminServiceImpl implements CompanyAdminService {

    private final WarehouseRepository warehouseRepository;
    private final VehicleRepository vehicleRepository;
    private final ShipmentRepository shipmentRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final PricingConfigRepository pricingConfigRepository;
    private final LogisticsCompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CompanyAdminServiceImpl(WarehouseRepository warehouseRepository,
                                   VehicleRepository vehicleRepository,
                                   ShipmentRepository shipmentRepository,
                                   DriverProfileRepository driverProfileRepository,
                                   PricingConfigRepository pricingConfigRepository,
                                   LogisticsCompanyRepository companyRepository,
                                   UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        this.warehouseRepository = warehouseRepository;
        this.vehicleRepository = vehicleRepository;
        this.shipmentRepository = shipmentRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.pricingConfigRepository = pricingConfigRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private LogisticsCompany getCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    // Warehouse CRUD
    @Override
    @Transactional
    public Warehouse createWarehouse(Long companyId, Warehouse warehouse) {
        warehouse.setCompany(getCompany(companyId));
        return warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional
    public Warehouse updateWarehouse(Long companyId, Long id, Warehouse warehouseDetails) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
        if (!warehouse.getCompany().getId().equals(companyId)) {
            throw new UnauthorizedAccessException("Unauthorized");
        }
        warehouse.setName(warehouseDetails.getName());
        warehouse.setAddress(warehouseDetails.getAddress());
        warehouse.setCity(warehouseDetails.getCity());
        warehouse.setState(warehouseDetails.getState());
        warehouse.setActive(warehouseDetails.isActive());
        return warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional
    public void deleteWarehouse(Long companyId, Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
        if (!warehouse.getCompany().getId().equals(companyId)) {
            throw new UnauthorizedAccessException("Unauthorized");
        }
        warehouseRepository.delete(warehouse);
    }

    @Override
    public List<Warehouse> getWarehouses(Long companyId) {
        return warehouseRepository.findByCompanyId(companyId);
    }

    // Vehicle CRUD
    @Override
    @Transactional
    public Vehicle createVehicle(Long companyId, Vehicle vehicle) {
        vehicle.setCompany(getCompany(companyId));
        
        if (vehicle.getDriver() != null && vehicle.getDriver().getId() != null) {
            Long driverId = vehicle.getDriver().getId();
            User driver = userRepository.findById(driverId)
                    .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
            
            if (!driver.getCompany().getId().equals(companyId)) {
                throw new UnauthorizedAccessException("Unauthorized: Driver belongs to another company");
            }
            
            vehicle.setDriver(driver);
            Vehicle savedVehicle = vehicleRepository.save(vehicle);
            
            DriverProfile profile = driverProfileRepository.findByUserId(driverId)
                    .orElseThrow(() -> new RuntimeException("Driver profile not found"));
            profile.setAssignedVehicle(savedVehicle);
            driverProfileRepository.save(profile);
            
            return savedVehicle;
        }
        
        return vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional
    public Vehicle updateVehicle(Long companyId, Long id, Vehicle vehicleDetails) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        if (!vehicle.getCompany().getId().equals(companyId)) {
            throw new UnauthorizedAccessException("Unauthorized");
        }
        vehicle.setPlateNumber(vehicleDetails.getPlateNumber());
        vehicle.setVehicleType(vehicleDetails.getVehicleType());
        vehicle.setMaxPayloadKg(vehicleDetails.getMaxPayloadKg());
        vehicle.setStatus(vehicleDetails.getStatus());
        return vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional
    public void deleteVehicle(Long companyId, Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        if (!vehicle.getCompany().getId().equals(companyId)) {
            throw new UnauthorizedAccessException("Unauthorized");
        }
        vehicleRepository.delete(vehicle);
    }

    @Override
    public List<Vehicle> getVehicles(Long companyId) {
        return vehicleRepository.findByCompanyId(companyId);
    }

    @Override
    @Transactional
    public void assignDriverToVehicle(Long companyId, Long vehicleId, Long driverId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        if (!vehicle.getCompany().getId().equals(companyId) || !driver.getCompany().getId().equals(companyId)) {
            throw new UnauthorizedAccessException("Unauthorized");
        }

        vehicle.setDriver(driver);
        vehicleRepository.save(vehicle);

        DriverProfile profile = driverProfileRepository.findByUserId(driverId)
                .orElseThrow(() -> new RuntimeException("Driver profile not found"));
        profile.setAssignedVehicle(vehicle);
        driverProfileRepository.save(profile);
    }

    // Shipments
    @Override
    public List<Shipment> getShipments(Long companyId, ShipmentStatus status, LocalDateTime start, LocalDateTime end) {
        return shipmentRepository.findByCompanyWithFilters(companyId, status, start, end);
    }

    // Drivers
    @Override
    @Transactional
    public DriverResponse createDriver(Long companyId, CreateDriverRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        LogisticsCompany company = getCompany(companyId);

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .accountType(AccountType.DRIVER)
                .company(company)
                .build();

        user = userRepository.save(user);

        DriverProfile profile = DriverProfile.builder()
                .user(user)
                .company(company)
                .licenseNumber(request.getLicenseNumber())
                .licenseExpiry(request.getLicenseExpiry())
                .ratingAverage(BigDecimal.ZERO)
                .totalDeliveries(0)
                .totalRatingCount(0)
                .build();

        driverProfileRepository.save(profile);

        return new DriverResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                profile.getRatingAverage(),
                profile.getTotalDeliveries(),
                null,
                null
        );
    }

    @Override
    public List<DriverResponse> getDrivers(Long companyId) {
        return driverProfileRepository.getDriversWithStatus(companyId);
    }

    // Reports
    @Override
    public List<RevenueReportResponse> getRevenueReport(Long companyId) {
        List<RevenueReportProjection> projections = shipmentRepository.getRevenueReport(companyId);
        return projections.stream()
                .map(p -> new RevenueReportResponse(p.getShipmentType(), p.getMonth(), p.getTotalFees()))
                .collect(Collectors.toList());
    }

    @Override
    public List<com.example.multi_tenant_logistic_app.repository.projection.DriverPerformanceProjection> getDriverPerformanceReport(Long companyId) {
        return driverProfileRepository.getDriverPerformanceReport(companyId);
    }

    @Override
    public List<com.example.multi_tenant_logistic_app.repository.projection.HeatMapProjection> getHeatMap(Long companyId) {
        return shipmentRepository.getHeatMap(companyId);
    }

    // Pricing
    @Override
    @Transactional
    public PricingConfig setPricingConfig(Long companyId, PricingConfig config) {
        config.setCompany(getCompany(companyId));
        return pricingConfigRepository.save(config);
    }
}

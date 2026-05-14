package com.example.multi_tenant_logistic_app.repository;

import com.example.multi_tenant_logistic_app.domain.entity.Vehicle;
import com.example.multi_tenant_logistic_app.domain.enumeration.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByCompanyId(Long companyId);
    List<Vehicle> findByCompanyIdAndStatus(Long companyId, VehicleStatus status);
    Optional<Vehicle> findByPlateNumber(String plateNumber);
}

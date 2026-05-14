package com.example.multi_tenant_logistic_app.repository;

import com.example.multi_tenant_logistic_app.domain.entity.DeliveryAssignment;
import com.example.multi_tenant_logistic_app.domain.enumeration.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, Long> {
    List<DeliveryAssignment> findByDriverId(Long driverId);
    List<DeliveryAssignment> findByDriverIdAndStatus(Long driverId, AssignmentStatus status);
    Optional<DeliveryAssignment> findByShipmentIdAndStatus(Long shipmentId, AssignmentStatus status);
}

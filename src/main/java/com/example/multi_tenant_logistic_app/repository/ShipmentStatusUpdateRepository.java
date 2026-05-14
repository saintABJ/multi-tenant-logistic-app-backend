package com.example.multi_tenant_logistic_app.repository;

import com.example.multi_tenant_logistic_app.domain.entity.ShipmentStatusUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentStatusUpdateRepository extends JpaRepository<ShipmentStatusUpdate, Long> {
    List<ShipmentStatusUpdate> findByShipmentIdOrderByUpdatedAtDesc(Long shipmentId);
}

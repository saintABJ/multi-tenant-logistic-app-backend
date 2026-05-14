package com.example.multi_tenant_logistic_app.repository;

import com.example.multi_tenant_logistic_app.domain.entity.Shipment;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus;
import com.example.multi_tenant_logistic_app.dto.responses.CompanyStatsResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long>, JpaSpecificationExecutor<Shipment> {
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    List<Shipment> findByCompanyId(Long companyId);
    List<Shipment> findByCustomerId(Long customerId);
    List<Shipment> findByCompanyIdAndStatus(Long companyId, ShipmentStatus status);

    @Query("SELECT new com.example.multi_tenant_logistic_app.dto.responses.CompanyStatsResponse(s.company.id, s.company.name, COUNT(s), SUM(s.totalFee)) " +
           "FROM Shipment s GROUP BY s.company.id, s.company.name")
    List<CompanyStatsResponse> getCompaniesWithStats();
    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.createdAt >= ?1")
    Long countShipmentsToday(java.time.LocalDateTime startOfDay);

    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.status = com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus.DELIVERED AND s.actualDeliveryAt >= ?1")
    Long countDeliveredThisMonth(java.time.LocalDateTime startOfMonth);

    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (actual_delivery_at - created_at)) / 3600) FROM shipments WHERE status = 'DELIVERED'", nativeQuery = true)
    Double getAverageDeliveryTimeHours();
    @Query(value = "SELECT shipment_type as shipmentType, CAST(EXTRACT(MONTH FROM created_at) AS integer) as month, SUM(total_fee) as totalFees FROM shipments WHERE company_id = ?1 GROUP BY shipment_type, EXTRACT(MONTH FROM created_at)", nativeQuery = true)
    List<com.example.multi_tenant_logistic_app.repository.projection.RevenueReportProjection> getRevenueReport(Long companyId);
    @Query("SELECT s FROM Shipment s WHERE s.company.id = ?1 " +
           "AND (?2 IS NULL OR s.status = ?2) " +
           "AND (?3 IS NULL OR s.createdAt >= ?3) " +
           "AND (?4 IS NULL OR s.createdAt <= ?4)")
    List<Shipment> findByCompanyWithFilters(Long companyId, ShipmentStatus status, java.time.LocalDateTime start, java.time.LocalDateTime end);
    List<Shipment> findByStatusNotAndCreatedAtBeforeAndSlaBreachedFalse(com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus status, java.time.LocalDateTime dateTime);
    @Query("SELECT s.recipientCity as city, COUNT(s) as count FROM Shipment s WHERE s.company.id = ?1 GROUP BY s.recipientCity")
    List<com.example.multi_tenant_logistic_app.repository.projection.HeatMapProjection> getHeatMap(Long companyId);
}

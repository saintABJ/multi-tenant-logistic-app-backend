package com.example.multi_tenant_logistic_app.repository;

import com.example.multi_tenant_logistic_app.domain.entity.DriverProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {
    Optional<DriverProfile> findByUserId(Long userId);
    List<DriverProfile> findByCompanyId(Long companyId);

    @org.springframework.data.jpa.repository.Query("SELECT new com.example.multi_tenant_logistic_app.dto.responses.DriverResponse(dp.user.id, dp.user.name, dp.user.email, dp.ratingAverage, dp.totalDeliveries, da.id, da.status) " +
           "FROM DriverProfile dp " +
           "LEFT JOIN DeliveryAssignment da ON da.driver.id = dp.user.id AND da.status = com.example.multi_tenant_logistic_app.domain.enumeration.AssignmentStatus.ACTIVE " +
           "WHERE dp.company.id = ?1")
    List<com.example.multi_tenant_logistic_app.dto.responses.DriverResponse> getDriversWithStatus(Long companyId);
    @org.springframework.data.jpa.repository.Query(value = "SELECT dp.user_id as driverId, u.name as name, dp.rating_average as rating, dp.total_deliveries as totalDeliveries, " +
           "CASE WHEN COUNT(s.id) = 0 THEN 0.0 ELSE COUNT(CASE WHEN s.actual_delivery_at <= s.created_at + interval '24 hours' THEN 1 END) * 100.0 / COUNT(s.id) END as onTimeRate " +
           "FROM driver_profiles dp " +
           "JOIN users u ON u.id = dp.user_id " +
           "LEFT JOIN delivery_assignments da ON da.driver_id = dp.user_id " +
           "LEFT JOIN shipments s ON s.id = da.shipment_id AND s.status = 'DELIVERED' " +
           "WHERE dp.company_id = ?1 " +
           "GROUP BY dp.user_id, u.name, dp.rating_average, dp.total_deliveries", nativeQuery = true)
    List<com.example.multi_tenant_logistic_app.repository.projection.DriverPerformanceProjection> getDriverPerformanceReport(Long companyId);
}

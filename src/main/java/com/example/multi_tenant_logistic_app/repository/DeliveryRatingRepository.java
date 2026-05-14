package com.example.multi_tenant_logistic_app.repository;

import com.example.multi_tenant_logistic_app.domain.entity.DeliveryRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRatingRepository extends JpaRepository<DeliveryRating, Long> {
    Optional<DeliveryRating> findByShipmentId(Long shipmentId);
    List<DeliveryRating> findByDriverId(Long driverId);

    @org.springframework.data.jpa.repository.Query("SELECT AVG(r.rating) FROM DeliveryRating r WHERE r.driver.id = ?1")
    Double getAverageRatingForDriver(Long driverId);
}

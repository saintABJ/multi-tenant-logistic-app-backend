package com.example.multi_tenant_logistic_app.scheduler;

import com.example.multi_tenant_logistic_app.domain.entity.Shipment;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus;
import com.example.multi_tenant_logistic_app.repository.ShipmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class SlaBreachScheduler {

    private final ShipmentRepository shipmentRepository;

    public SlaBreachScheduler(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void detectSlaBreaches() {
        log.info("Running SLA breach detection job...");

        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<Shipment> breachedShipments = shipmentRepository.findByStatusNotAndCreatedAtBeforeAndSlaBreachedFalse(
                ShipmentStatus.DELIVERED, threshold);

        if (!breachedShipments.isEmpty()) {
            log.warn("Found {} shipments breaching SLA", breachedShipments.size());
            for (Shipment shipment : breachedShipments) {
                shipment.setSlaBreached(true);
                shipmentRepository.save(shipment);
                log.warn("Shipment {} marked as SLA breached", shipment.getTrackingNumber());
            }
        }
    }
    @Scheduled(cron = "0 0 1 * * ?")
    public void nightlyCleanup() {
        log.info("Running nightly cleanup job...");
    }
}

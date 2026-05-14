package com.example.multi_tenant_logistic_app.service.impl;

import com.example.multi_tenant_logistic_app.domain.entity.*;
import com.example.multi_tenant_logistic_app.domain.enumeration.AssignmentStatus;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus;
import com.example.multi_tenant_logistic_app.dto.requests.BookShipmentRequest;
import com.example.multi_tenant_logistic_app.dto.requests.RateDeliveryRequest;
import com.example.multi_tenant_logistic_app.exception.BadRequestException;
import com.example.multi_tenant_logistic_app.exception.ResourceNotFoundException;
import com.example.multi_tenant_logistic_app.exception.UnauthorizedAccessException;
import com.example.multi_tenant_logistic_app.repository.*;
import com.example.multi_tenant_logistic_app.service.CustomerService;
import com.example.multi_tenant_logistic_app.service.ShipmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final ShipmentService shipmentService;
    private final ShipmentRepository shipmentRepository;
    private final DeliveryRatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final DeliveryAssignmentRepository assignmentRepository;
    private final DriverProfileRepository driverProfileRepository;

    public CustomerServiceImpl(ShipmentService shipmentService,
                               ShipmentRepository shipmentRepository,
                               DeliveryRatingRepository ratingRepository,
                               UserRepository userRepository,
                               DeliveryAssignmentRepository assignmentRepository,
                               DriverProfileRepository driverProfileRepository) {
        this.shipmentService = shipmentService;
        this.shipmentRepository = shipmentRepository;
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.driverProfileRepository = driverProfileRepository;
    }

    @Override
    public Shipment bookShipment(Long customerId, Long companyId, BookShipmentRequest request) {
        return shipmentService.bookShipment(customerId, companyId, request);
    }

    @Override
    public List<Shipment> getShipmentHistory(Long customerId) {
        return shipmentService.getCustomerShipments(customerId);
    }

    @Override
    @Transactional
    public void rateDelivery(Long customerId, RateDeliveryRequest request) {
        Shipment shipment = shipmentRepository.findById(request.getShipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        if (!shipment.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedAccessException("Unauthorized to rate this shipment");
        }

        if (shipment.getStatus() != ShipmentStatus.DELIVERED) {
            throw new BadRequestException("Can only rate delivered shipments");
        }

        if (ratingRepository.findByShipmentId(shipment.getId()).isPresent()) {
            throw new BadRequestException("Shipment already rated");
        }

        List<DeliveryAssignment> assignments = assignmentRepository.findByDriverId(null);
        DeliveryAssignment assignment = assignmentRepository.findByShipmentIdAndStatus(shipment.getId(), AssignmentStatus.COMPLETED)
                .orElseThrow(() -> new ResourceNotFoundException("Completed assignment not found for shipment"));

        User driver = assignment.getDriver();
        User customer = shipment.getCustomer();

        DeliveryRating rating = DeliveryRating.builder()
                .shipment(shipment)
                .driver(driver)
                .customer(customer)
                .rating(request.getRating())
                .comment(request.getReviewText())
                .build();

        ratingRepository.save(rating);

        // Update driver rating
        Double avgRating = ratingRepository.getAverageRatingForDriver(driver.getId());
        
        DriverProfile profile = driverProfileRepository.findByUserId(driver.getId())
                .orElseThrow(() -> new RuntimeException("Driver profile not found"));
        
        profile.setRatingAverage(BigDecimal.valueOf(avgRating));
        profile.setTotalDeliveries(profile.getTotalDeliveries() + 1);
        
        driverProfileRepository.save(profile);
    }
}

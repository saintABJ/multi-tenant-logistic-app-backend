package com.example.multi_tenant_logistic_app.service;

import com.example.multi_tenant_logistic_app.domain.entity.Shipment;
import com.example.multi_tenant_logistic_app.dto.requests.BookShipmentRequest;
import com.example.multi_tenant_logistic_app.dto.responses.ShipmentTrackingResponse;

import java.util.List;

public interface ShipmentService {
    Shipment bookShipment(Long customerId, Long companyId, BookShipmentRequest request);
    ShipmentTrackingResponse trackShipment(String trackingNumber);
    List<Shipment> getCustomerShipments(Long customerId);
}

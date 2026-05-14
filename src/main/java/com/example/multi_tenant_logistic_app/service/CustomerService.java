package com.example.multi_tenant_logistic_app.service;

import com.example.multi_tenant_logistic_app.domain.entity.Shipment;
import com.example.multi_tenant_logistic_app.dto.requests.BookShipmentRequest;
import com.example.multi_tenant_logistic_app.dto.requests.RateDeliveryRequest;

import java.util.List;

public interface CustomerService {
    Shipment bookShipment(Long customerId, Long companyId, BookShipmentRequest request);
    List<Shipment> getShipmentHistory(Long customerId);
    void rateDelivery(Long customerId, RateDeliveryRequest request);
}

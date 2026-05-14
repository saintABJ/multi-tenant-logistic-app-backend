package com.example.multi_tenant_logistic_app.dto.requests;

import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookShipmentRequest {

    private Long originWarehouseId;

    @NotBlank(message = "Sender name is required")
    private String senderName;

    @NotBlank(message = "Sender phone is required")
    private String senderPhone;

    @NotBlank(message = "Sender address is required")
    private String senderAddress;

    @NotBlank(message = "Recipient name is required")
    private String recipientName;

    @NotBlank(message = "Recipient phone is required")
    private String recipientPhone;

    @NotBlank(message = "Recipient address is required")
    private String recipientAddress;

    @NotBlank(message = "Recipient city is required")
    private String recipientCity;

    @NotBlank(message = "Recipient state is required")
    private String recipientState;

    @NotBlank(message = "Package description is required")
    private String packageDescription;

    @NotNull(message = "Weight is required")
    private BigDecimal weightKg;

    private BigDecimal declaredValue;

    @NotNull(message = "Shipment type is required")
    private ShipmentType shipmentType;
}

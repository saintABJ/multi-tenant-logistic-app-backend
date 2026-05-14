package com.example.multi_tenant_logistic_app.dto.responses;

import com.example.multi_tenant_logistic_app.domain.entity.ShipmentStatusUpdate;
import com.example.multi_tenant_logistic_app.domain.enumeration.ShipmentStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ShipmentTrackingResponse {
    private String trackingNumber;
    private ShipmentStatus currentStatus;
    private List<StatusUpdateDto> history;

    @Data
    @Builder
    public static class StatusUpdateDto {
        private ShipmentStatus status;
        private String note;
        private String location;
        private String updatedAt;
    }
}

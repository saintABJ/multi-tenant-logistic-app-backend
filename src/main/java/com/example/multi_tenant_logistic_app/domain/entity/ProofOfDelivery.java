package com.example.multi_tenant_logistic_app.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "proof_of_delivery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProofOfDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false, unique = true)
    private Shipment shipment;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_assignment_id", nullable = false, unique = true)
    private DeliveryAssignment deliveryAssignment;

    @Column(name = "recipient_name_signed", nullable = false)
    private String recipientNameSigned;

    @Column(name = "delivery_note")
    private String deliveryNote;

    @Column(name = "delivery_image_url")
    private String deliveryImageUrl;

    @CreationTimestamp
    @Column(name = "delivered_at", updatable = false)
    private LocalDateTime deliveredAt;
}

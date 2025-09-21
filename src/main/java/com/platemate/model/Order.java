package com.platemate.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.platemate.enums.OrderStatus;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    // ---------------- Relationships ----------------

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer; // FK → customers.customer_id

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private TiffinProvider provider; // FK → tiffin_providers.provider_id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_partner_id")
    private DeliveryPartner deliveryPartner; // FK → delivery_partners.delivery_partner_id

    // ---------------- Enums ----------------

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    // ---------------- Other Fields ----------------

    @Column(name = "cart_item_ids", nullable = false, columnDefinition = "TEXT")
    private String cartItemIds; // JSON array of cart_item_id (stored as String)

    @Column(name = "delivery_fee", nullable = false)
    private Double deliveryFee = 0.00;

    @Column(name = "platform_commission", nullable = false)
    private Double platformCommission = 0.00;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "delivery_address", nullable = false, columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "order_time", nullable = false, updatable = false)
    private LocalDateTime orderTime;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "delivery_time")
    private LocalDateTime deliveryTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    // ---------------- Lifecycle Hooks ----------------

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        orderTime = now;
        if (orderStatus == null) {
            orderStatus = OrderStatus.PENDING;
        }
        if (deliveryFee == null) deliveryFee = 0.00;
        if (platformCommission == null) platformCommission = 0.00;
        if (isDeleted == null) isDeleted = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
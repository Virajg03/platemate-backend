package com.platemate.model;

import java.time.LocalDateTime;

import com.platemate.enums.PayoutStatus;
import com.platemate.enums.RecipientType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "payouts")
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payout_id")
    private Long payoutId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false)
    private RecipientType recipientType;

    @Column(name = "order_ids", nullable = false, columnDefinition = "TEXT")
    private String orderIds; // JSON array of order_id

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "commission_deducted", nullable = false)
    private Double commissionDeducted;

    @Column(name = "net_amount", nullable = false)
    private Double netAmount;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PayoutStatus status;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "payout_time")
    private LocalDateTime payoutTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (isDeleted == null) isDeleted = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    } 

    public Payout() {}

    public Payout(Long recipientId, RecipientType recipientType, String orderIds, Double amount,
            Double commissionDeducted, Double netAmount, PayoutStatus status, String transactionId,
            LocalDateTime payoutTime, LocalDateTime createdAt, LocalDateTime updatedAt, Boolean isDeleted) {
        this.recipientId = recipientId;
        this.recipientType = recipientType;
        this.orderIds = orderIds;
        this.amount = amount;
        this.commissionDeducted = commissionDeducted;
        this.netAmount = netAmount;
        this.status = status;
        this.transactionId = transactionId;
        this.payoutTime = payoutTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    public Long getPayoutId() {
        return payoutId;
    }

    public void setPayoutId(Long payoutId) {
        this.payoutId = payoutId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public RecipientType getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(RecipientType recipientType) {
        this.recipientType = recipientType;
    }

    public String getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(String orderIds) {
        this.orderIds = orderIds;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getCommissionDeducted() {
        return commissionDeducted;
    }

    public void setCommissionDeducted(Double commissionDeducted) {
        this.commissionDeducted = commissionDeducted;
    }

    public Double getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(Double netAmount) {
        this.netAmount = netAmount;
    }

    public PayoutStatus getStatus() {
        return status;
    }

    public void setStatus(PayoutStatus status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getPayoutTime() {
        return payoutTime;
    }

    public void setPayoutTime(LocalDateTime payoutTime) {
        this.payoutTime = payoutTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }


}


package com.platemate.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "driving_licenses")
public class DrivingLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dl_id")
    private Long dlId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_partner_id", nullable = false)
    private DeliveryPartner deliveryPartner;

    @Column(name = "dl_number", nullable = false, unique = true, length = 20)
    private String dlNumber;

    @Column(name = "dl_image_front_url", nullable = false, length = 500)
    private String dlImageFrontUrl;

    @Column(name = "dl_image_back_url", nullable = false, length = 500)
    private String dlImageBackUrl;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "issuing_authority", nullable = false, length = 100)
    private String issuingAuthority;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    // ---------------- Lifecycle Hooks ----------------

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public DrivingLicense() {
    }

    public DrivingLicense(Long dlId, DeliveryPartner deliveryPartner, String dlNumber, String dlImageFrontUrl,
            String dlImageBackUrl, LocalDate issueDate, LocalDate expiryDate, String issuingAuthority,
            Boolean isVerified, LocalDateTime createdAt, LocalDateTime updatedAt, Boolean isDeleted) {
        this.dlId = dlId;
        this.deliveryPartner = deliveryPartner;
        this.dlNumber = dlNumber;
        this.dlImageFrontUrl = dlImageFrontUrl;
        this.dlImageBackUrl = dlImageBackUrl;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.issuingAuthority = issuingAuthority;
        this.isVerified = isVerified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    // ---------------- Getters & Setters ----------------

    public Long getDlId() {
        return dlId;
    }

    public void setDlId(Long dlId) {
        this.dlId = dlId;
    }

    public DeliveryPartner getDeliveryPartner() {
        return deliveryPartner;
    }

    public void setDeliveryPartner(DeliveryPartner deliveryPartner) {
        this.deliveryPartner = deliveryPartner;
    }

    public String getDlNumber() {
        return dlNumber;
    }

    public void setDlNumber(String dlNumber) {
        this.dlNumber = dlNumber;
    }

    public String getDlImageFrontUrl() {
        return dlImageFrontUrl;
    }

    public void setDlImageFrontUrl(String dlImageFrontUrl) {
        this.dlImageFrontUrl = dlImageFrontUrl;
    }

    public String getDlImageBackUrl() {
        return dlImageBackUrl;
    }

    public void setDlImageBackUrl(String dlImageBackUrl) {
        this.dlImageBackUrl = dlImageBackUrl;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getIssuingAuthority() {
        return issuingAuthority;
    }

    public void setIssuingAuthority(String issuingAuthority) {
        this.issuingAuthority = issuingAuthority;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
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


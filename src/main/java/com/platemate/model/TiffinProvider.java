package com.platemate.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "tiffin_providers")
public class TiffinProvider {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tiffin_provider_id")
    private Long tiffinProviderId;

     // ---------------- Relationships ----------------

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;   // FK → users.user_id

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private DeliveryZone zone;   // FK → delivery_zones.zone_id

    // ---------------- Other fields ----------------

    @Column(name = "business_name", nullable = false, length = 255)
    private String businessName;

    @Column(name = "description")
    private String description;

    @Column(name = "commission_rate", nullable = false)
    private Double commissionRate = 0.00;

    @Column(name = "provides_delivery", nullable = false)
    private Boolean providesDelivery = false;

    @Column(name = "delivery_radius")
    private Double deliveryRadius;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Transient
    private List<RatingReview> ratings;

    @Transient
    private Image profileImage;

    @Transient
    private List<Image> placeImages;

    // TODO: Add findByRatingTypeAndTargetId in ratingReview repo.
    // Commented For temporary purposes.
    // public void loadRatings(RatingReviewRepository repo) {
    //     this.ratings = repo.findByRatingTypeAndTargetId(
    //         RatingType.ITEM_RATING, this.itemId
    //     );
    // }

    public List<RatingReview> getRatings() {
        return ratings;
    }

    public void setRatings(List<RatingReview> ratings) {
        this.ratings = ratings;
    }

    public Image getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Image profileImage) {
        this.profileImage = profileImage;
    }

    public List<Image> getPlaceImages() {
        return placeImages;
    }

    public void setPlaceImages(List<Image> placeImages) {
        this.placeImages = placeImages;
    }

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    public TiffinProvider() {}

    public TiffinProvider(User user, DeliveryZone zone, String businessName, String description,
            Double commissionRate, Boolean providesDelivery, Double deliveryRadius, Boolean isVerified,
            LocalDateTime createdAt, LocalDateTime updatedAt, Boolean isDeleted) {
        this.user = user;
        this.zone = zone;
        this.businessName = businessName;
        this.description = description;
        this.commissionRate = commissionRate;
        this.providesDelivery = providesDelivery;
        this.deliveryRadius = deliveryRadius;
        this.isVerified = isVerified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    public Long getTiffinProviderId() {
        return tiffinProviderId;
    }

    public void setTiffinProviderId(Long tiffinProviderId) {
        this.tiffinProviderId = tiffinProviderId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DeliveryZone getZone() {
        return zone;
    }

    public void setZone(DeliveryZone zone) {
        this.zone = zone;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(Double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public Boolean getProvidesDelivery() {
        return providesDelivery;
    }

    public void setProvidesDelivery(Boolean providesDelivery) {
        this.providesDelivery = providesDelivery;
    }

    public Double getDeliveryRadius() {
        return deliveryRadius;
    }

    public void setDeliveryRadius(Double deliveryRadius) {
        this.deliveryRadius = deliveryRadius;
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
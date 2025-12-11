package com.example.platemate;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class DeliveryPartner implements Serializable {
    private static final long serialVersionUID = 1L;
    @SerializedName("id")
    private Long id;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("providerId")
    private Long providerId;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("vehicleType")
    private String vehicleType; // BIKE, SCOOTER, BICYCLE, CAR

    @SerializedName("serviceArea")
    private String serviceArea;

    @SerializedName("isAvailable")
    private Boolean isAvailable;

    // Transient fields for user creation (not serialized in response)
    private transient String username;
    private transient String email;
    private transient String password;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    // Getters and setters for user credentials (transient)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}


package com.example.platemate;

import com.google.gson.annotations.SerializedName;

public class DeliveryPartnerCreateRequest {
    @SerializedName("userId")
    private Long userId;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("vehicleType")
    private String vehicleType;

    @SerializedName("commissionRate")
    private Double commissionRate;

    @SerializedName("serviceArea")
    private String serviceArea;

    public DeliveryPartnerCreateRequest() {}

    public DeliveryPartnerCreateRequest(Long userId, String fullName, String vehicleType, Double commissionRate, String serviceArea) {
        this.userId = userId;
        this.fullName = fullName;
        this.vehicleType = vehicleType;
        this.commissionRate = commissionRate;
        this.serviceArea = serviceArea;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(Double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }
}


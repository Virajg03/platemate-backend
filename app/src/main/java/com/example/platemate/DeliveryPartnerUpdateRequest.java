package com.example.platemate;

import com.google.gson.annotations.SerializedName;

public class DeliveryPartnerUpdateRequest {
    @SerializedName("fullName")
    private String fullName;

    @SerializedName("vehicleType")
    private String vehicleType;

    @SerializedName("serviceArea")
    private String serviceArea;

    @SerializedName("isAvailable")
    private Boolean isAvailable;

    public DeliveryPartnerUpdateRequest() {}

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
}


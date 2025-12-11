package com.example.platemate;

import com.google.gson.annotations.SerializedName;

public class DeliveryPartnerCreateRequest {
    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("vehicleType")
    private String vehicleType;

    @SerializedName("serviceArea")
    private String serviceArea;

    public DeliveryPartnerCreateRequest() {}

    public DeliveryPartnerCreateRequest(String username, String email, String password, String fullName, String vehicleType, String serviceArea) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.vehicleType = vehicleType;
        this.serviceArea = serviceArea;
    }

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
}


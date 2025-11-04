package com.platemate.dto;

import java.math.BigDecimal;

import com.platemate.enums.VehicleType;

public class DeliveryPartnerDtos {
    public static class CreateRequest {
        private Long userId;
        private String fullName;
        private VehicleType vehicleType;
        private BigDecimal commissionRate;
        private String serviceArea;
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public VehicleType getVehicleType() { return vehicleType; }
        public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
        public BigDecimal getCommissionRate() { return commissionRate; }
        public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
        public String getServiceArea() { return serviceArea; }
        public void setServiceArea(String serviceArea) { this.serviceArea = serviceArea; }
    }

    public static class UpdateRequest {
        private String fullName;
        private VehicleType vehicleType;
        private BigDecimal commissionRate;
        private String serviceArea;
        private Boolean isAvailable;
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public VehicleType getVehicleType() { return vehicleType; }
        public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
        public BigDecimal getCommissionRate() { return commissionRate; }
        public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
        public String getServiceArea() { return serviceArea; }
        public void setServiceArea(String serviceArea) { this.serviceArea = serviceArea; }
        public Boolean getIsAvailable() { return isAvailable; }
        public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    }

    public static class Response {
        private Long id;
        private Long userId;
        private String fullName;
        private VehicleType vehicleType;
        private BigDecimal commissionRate;
        private String serviceArea;
        private Boolean isAvailable;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public VehicleType getVehicleType() { return vehicleType; }
        public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
        public BigDecimal getCommissionRate() { return commissionRate; }
        public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
        public String getServiceArea() { return serviceArea; }
        public void setServiceArea(String serviceArea) { this.serviceArea = serviceArea; }
        public Boolean getIsAvailable() { return isAvailable; }
        public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    }
}



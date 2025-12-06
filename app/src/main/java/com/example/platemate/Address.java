package com.example.platemate;

import com.google.gson.annotations.SerializedName;

public class Address {
    @SerializedName("street1")
    private String street1;
    
    @SerializedName("street2")
    private String street2;
    
    @SerializedName("city")
    private String city;
    
    @SerializedName("state")
    private String state;
    
    @SerializedName("pincode")
    private String pincode;
    
    private String country;
    private Double latitude;
    private Double longitude;

    public Address() {
    }

    // For backward compatibility, provide getStreet() that returns street1
    public String getStreet() {
        return street1;
    }

    public void setStreet(String street) {
        this.street1 = street;
    }

    public String getStreet1() {
        return street1;
    }

    public void setStreet1(String street1) {
        this.street1 = street1;
    }

    public String getStreet2() {
        return street2;
    }

    public void setStreet2(String street2) {
        this.street2 = street2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    // For backward compatibility, provide getZipCode() that returns pincode
    public String getZipCode() {
        return pincode;
    }

    public void setZipCode(String zipCode) {
        this.pincode = zipCode;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}


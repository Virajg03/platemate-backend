package com.example.platemate;

import com.google.gson.annotations.SerializedName;

public class UpdateAddressRequest {
    @SerializedName("street")
    private String street;
    
    @SerializedName("city")
    private String city;
    
    @SerializedName("state")
    private String state;
    
    @SerializedName("zipCode")
    private String zipCode;
    
    @SerializedName("country")
    private String country;
    
    public UpdateAddressRequest(String street, String city, String state, String zipCode) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = "India"; // Default country
    }
    
    public UpdateAddressRequest(String street, String city, String state, String zipCode, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }
    
    // Getters and Setters
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}


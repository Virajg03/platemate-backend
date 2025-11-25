package com.example.platemate;

import com.google.gson.annotations.SerializedName;

public class AddressRequest {
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
    
    @SerializedName("address_type")
    private String addressType; // Valid values: "OTHER", "OFFICE", "HOME", "BUSINESS"
    
    public AddressRequest(String street1, String street2, String city, String state, String pincode, String addressType) {
        this.street1 = street1;
        this.street2 = street2;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.addressType = addressType;
    }
    
    // Getters and Setters
    public String getStreet1() { return street1; }
    public void setStreet1(String street1) { this.street1 = street1; }
    
    public String getStreet2() { return street2; }
    public void setStreet2(String street2) { this.street2 = street2; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    
    public String getAddressType() { return addressType; }
    public void setAddressType(String addressType) { this.addressType = addressType; }
}


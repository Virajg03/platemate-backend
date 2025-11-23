package com.example.platemate;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("phoneNumber")
    private String phoneNumber;
    
    @SerializedName("role")
    private String role;
    
    @SerializedName("address")
    private Address address;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
}


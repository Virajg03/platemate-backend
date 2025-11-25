package com.example.platemate;

import com.google.gson.annotations.SerializedName;

public class Customer {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("profileImageUrl")
    private String profileImageUrl;
    
    @SerializedName("profileImageBase64")
    private String profileImageBase64;
    
    @SerializedName("profileImageId")
    private Long profileImageId;
    
    @SerializedName("address")
    private Address address;
    
    @SerializedName("fullName")
    private String fullName;
    
    @SerializedName("dateOfBirth")
    private String dateOfBirth;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    
    public String getProfileImageBase64() { return profileImageBase64; }
    public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }
    
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    
    public Long getProfileImageId() { return profileImageId; }
    public void setProfileImageId(Long profileImageId) { this.profileImageId = profileImageId; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
}


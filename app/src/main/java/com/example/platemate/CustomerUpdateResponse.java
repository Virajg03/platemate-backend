package com.example.platemate;

import com.google.gson.annotations.SerializedName;

public class CustomerUpdateResponse {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("userId")
    private Long userId;
    
    @SerializedName("fullName")
    private String fullName;
    
    @SerializedName("dateOfBirth")
    private String dateOfBirth;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
}


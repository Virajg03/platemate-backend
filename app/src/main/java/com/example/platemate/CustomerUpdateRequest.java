package com.example.platemate;

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomerUpdateRequest {
    @SerializedName("fullName")
    private String fullName;
    
    @SerializedName("dateOfBirth")
    private String dateOfBirth; // Format: "yyyy-MM-dd"
    
    public CustomerUpdateRequest() {}
    
    public CustomerUpdateRequest(String fullName, Date dateOfBirth) {
        this.fullName = fullName;
        if (dateOfBirth != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            this.dateOfBirth = sdf.format(dateOfBirth);
        }
    }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public void setDateOfBirth(Date dateOfBirth) {
        if (dateOfBirth != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            this.dateOfBirth = sdf.format(dateOfBirth);
        } else {
            this.dateOfBirth = null;
        }
    }
}


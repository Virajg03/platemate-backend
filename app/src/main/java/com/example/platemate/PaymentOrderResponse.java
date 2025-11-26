package com.example.platemate;

import com.google.gson.annotations.SerializedName;

public class PaymentOrderResponse {
    @SerializedName("razorpayOrderId")
    private String razorpayOrderId;
    
    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }
    
    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }
}


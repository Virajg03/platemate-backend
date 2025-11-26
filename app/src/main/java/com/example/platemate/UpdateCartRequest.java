package com.example.platemate;

import com.google.gson.annotations.SerializedName;

public class UpdateCartRequest {
    @SerializedName("quantity")
    private Integer quantity;
    
    @SerializedName("specialInstructions")
    private String specialInstructions;
    
    public UpdateCartRequest(Integer quantity, String specialInstructions) {
        this.quantity = quantity;
        this.specialInstructions = specialInstructions;
    }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
}





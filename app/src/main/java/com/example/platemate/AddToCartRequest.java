package com.example.platemate;

import com.google.gson.annotations.SerializedName;

public class AddToCartRequest {
    @SerializedName("menuItemId")
    private Long menuItemId;
    
    @SerializedName("quantity")
    private Integer quantity;
    
    @SerializedName("specialInstructions")
    private String specialInstructions;
    
    public AddToCartRequest(Long menuItemId, Integer quantity, String specialInstructions) {
        this.menuItemId = menuItemId;
        this.quantity = quantity;
        this.specialInstructions = specialInstructions;
    }
    
    public Long getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
}





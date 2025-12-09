package com.example.platemate;

import com.google.gson.annotations.SerializedName;

public class CartItem {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("menuItemId")
    private Long menuItemId;
    
    @SerializedName("itemName")
    private String itemName;
    
    @SerializedName("quantity")
    private Integer quantity;
    
    @SerializedName("itemPrice")
    private Double itemPrice;
    
    @SerializedName("itemTotal")
    private Double itemTotal;
    
    @SerializedName("specialInstructions")
    private String specialInstructions;
    
    @SerializedName("providerId")
    private Long providerId;
    
    @SerializedName("providerName")
    private String providerName;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public Double getItemPrice() { return itemPrice; }
    public void setItemPrice(Double itemPrice) { this.itemPrice = itemPrice; }
    
    public Double getItemTotal() { return itemTotal; }
    public void setItemTotal(Double itemTotal) { this.itemTotal = itemTotal; }
    
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
}














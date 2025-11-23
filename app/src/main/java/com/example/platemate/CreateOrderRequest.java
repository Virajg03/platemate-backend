package com.example.platemate;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CreateOrderRequest {
    @SerializedName("cartItemIds")
    private List<Long> cartItemIds;
    
    @SerializedName("deliveryAddress")
    private String deliveryAddress;
    
    @SerializedName("deliveryFee")
    private Double deliveryFee;
    
    public CreateOrderRequest(List<Long> cartItemIds, String deliveryAddress, Double deliveryFee) {
        this.cartItemIds = cartItemIds;
        this.deliveryAddress = deliveryAddress;
        this.deliveryFee = deliveryFee;
    }
    
    public List<Long> getCartItemIds() { return cartItemIds; }
    public void setCartItemIds(List<Long> cartItemIds) { this.cartItemIds = cartItemIds; }
    
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    
    public Double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }
}


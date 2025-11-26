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
    
    @SerializedName("paymentMethod")
    private String paymentMethod;
    
    public CreateOrderRequest(List<Long> cartItemIds, String deliveryAddress, Double deliveryFee) {
        this.cartItemIds = cartItemIds;
        this.deliveryAddress = deliveryAddress;
        this.deliveryFee = deliveryFee;
        this.paymentMethod = "CASH"; // Default to COD
    }
    
    public CreateOrderRequest(List<Long> cartItemIds, String deliveryAddress, Double deliveryFee, String paymentMethod) {
        this.cartItemIds = cartItemIds;
        this.deliveryAddress = deliveryAddress;
        this.deliveryFee = deliveryFee;
        this.paymentMethod = paymentMethod;
    }
    
    public List<Long> getCartItemIds() { return cartItemIds; }
    public void setCartItemIds(List<Long> cartItemIds) { this.cartItemIds = cartItemIds; }
    
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    
    public Double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}





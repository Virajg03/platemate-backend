package com.example.platemate;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Order {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("customerId")
    private Long customerId;
    
    @SerializedName("providerId")
    private Long providerId;
    
    @SerializedName("providerName")
    private String providerName;
    
    @SerializedName("deliveryPartnerId")
    private Long deliveryPartnerId;
    
    @SerializedName("deliveryPartnerName")
    private String deliveryPartnerName;
    
    @SerializedName("orderStatus")
    private String orderStatus; // PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    
    @SerializedName("cartItems")
    private List<OrderItem> cartItems;
    
    @SerializedName("subtotal")
    private Double subtotal;
    
    @SerializedName("deliveryFee")
    private Double deliveryFee;
    
    @SerializedName("platformCommission")
    private Double platformCommission;
    
    @SerializedName("totalAmount")
    private Double totalAmount;
    
    @SerializedName("deliveryAddress")
    private String deliveryAddress;
    
    @SerializedName("orderTime")
    private String orderTime;
    
    @SerializedName("estimatedDeliveryTime")
    private String estimatedDeliveryTime;
    
    @SerializedName("deliveryTime")
    private String deliveryTime;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    
    public Long getDeliveryPartnerId() { return deliveryPartnerId; }
    public void setDeliveryPartnerId(Long deliveryPartnerId) { this.deliveryPartnerId = deliveryPartnerId; }
    
    public String getDeliveryPartnerName() { return deliveryPartnerName; }
    public void setDeliveryPartnerName(String deliveryPartnerName) { this.deliveryPartnerName = deliveryPartnerName; }
    
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    
    public List<OrderItem> getCartItems() { return cartItems; }
    public void setCartItems(List<OrderItem> cartItems) { this.cartItems = cartItems; }
    
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    
    public Double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }
    
    public Double getPlatformCommission() { return platformCommission; }
    public void setPlatformCommission(Double platformCommission) { this.platformCommission = platformCommission; }
    
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    
    public String getOrderTime() { return orderTime; }
    public void setOrderTime(String orderTime) { this.orderTime = orderTime; }
    
    public String getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public void setEstimatedDeliveryTime(String estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }
    
    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }
    
    // Inner class for OrderItem
    public static class OrderItem {
        @SerializedName("cartItemId")
        private Long cartItemId;
        
        @SerializedName("itemName")
        private String itemName;
        
        @SerializedName("quantity")
        private Integer quantity;
        
        @SerializedName("itemPrice")
        private Double itemPrice;
        
        @SerializedName("itemTotal")
        private Double itemTotal;
        
        public Long getCartItemId() { return cartItemId; }
        public void setCartItemId(Long cartItemId) { this.cartItemId = cartItemId; }
        
        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public Double getItemPrice() { return itemPrice; }
        public void setItemPrice(Double itemPrice) { this.itemPrice = itemPrice; }
        
        public Double getItemTotal() { return itemTotal; }
        public void setItemTotal(Double itemTotal) { this.itemTotal = itemTotal; }
    }
}









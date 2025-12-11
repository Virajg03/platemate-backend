package com.example.platemate;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class CartSummary {
    @SerializedName("items")
    private List<CartItem> items;
    
    @SerializedName("subtotal")
    private Double subtotal;
    
    @SerializedName("totalItems")
    private Integer totalItems;
    
    @SerializedName("groupedByProvider")
    private Map<Long, List<CartItem>> groupedByProvider;
    
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
    
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    
    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }
    
    public Map<Long, List<CartItem>> getGroupedByProvider() { return groupedByProvider; }
    public void setGroupedByProvider(Map<Long, List<CartItem>> groupedByProvider) { this.groupedByProvider = groupedByProvider; }
}


















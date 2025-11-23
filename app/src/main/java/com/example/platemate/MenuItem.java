package com.example.platemate;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MenuItem {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("categoryId")
    private Long categoryId;
    
    @SerializedName("categoryName")
    private String categoryName;
    
    @SerializedName("itemName")
    private String itemName;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("price")
    private Double price;
    
    @SerializedName("ingredients")
    private String ingredients;
    
    @SerializedName("mealType")
    private String mealType; // VEG, NON_VEG, JAIN
    
    @SerializedName("providerId")
    private Long providerId;
    
    @SerializedName("providerName")
    private String providerName;
    
    @SerializedName("providerBusinessName")
    private String providerBusinessName;
    
    @SerializedName("imageBase64List")
    private List<String> imageBase64List;
    
    @SerializedName("imageFileTypeList")
    private List<String> imageFileTypeList;
    
    // Pagination fields
    @SerializedName("page")
    private Integer page;
    
    @SerializedName("size")
    private Integer size;
    
    @SerializedName("totalElements")
    private Long totalElements;
    
    @SerializedName("totalPages")
    private Integer totalPages;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
    
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    
    public String getProviderBusinessName() { return providerBusinessName; }
    public void setProviderBusinessName(String providerBusinessName) { this.providerBusinessName = providerBusinessName; }
    
    public List<String> getImageBase64List() { return imageBase64List; }
    public void setImageBase64List(List<String> imageBase64List) { this.imageBase64List = imageBase64List; }
    
    public List<String> getImageFileTypeList() { return imageFileTypeList; }
    public void setImageFileTypeList(List<String> imageFileTypeList) { this.imageFileTypeList = imageFileTypeList; }
    
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
    
    public Long getTotalElements() { return totalElements; }
    public void setTotalElements(Long totalElements) { this.totalElements = totalElements; }
    
    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
    
    // Helper methods
    public String getFirstImageBase64() {
        if (imageBase64List != null && !imageBase64List.isEmpty()) {
            return imageBase64List.get(0);
        }
        return null;
    }
    
    public String getFirstImageFileType() {
        if (imageFileTypeList != null && !imageFileTypeList.isEmpty()) {
            return imageFileTypeList.get(0);
        }
        return null;
    }
}



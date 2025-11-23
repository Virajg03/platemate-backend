package com.example.platemate;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("categoryName")
    private String categoryName;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("imageBase64")
    private String imageBase64;
    
    @SerializedName("imageFileType")
    private String imageFileType;
    
    // Legacy fields for backward compatibility
    private String name;
    private int iconResId; // Resource ID for the icon drawable
    private String iconUrl; // Optional: URL for category icon

    public Category() {}

    // Legacy constructor
    public Category(String name, int iconResId) {
        this.name = name;
        this.categoryName = name;
        this.iconResId = iconResId;
    }

    public Category(String name, String iconUrl) {
        this.name = name;
        this.categoryName = name;
        this.iconUrl = iconUrl;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName != null ? categoryName : name;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
        this.name = categoryName; // Keep name in sync
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getImageFileType() {
        return imageFileType;
    }

    public void setImageFileType(String imageFileType) {
        this.imageFileType = imageFileType;
    }

    // Legacy getters for compatibility
    public String getName() {
        return getCategoryName();
    }

    public void setName(String name) {
        this.name = name;
        this.categoryName = name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}


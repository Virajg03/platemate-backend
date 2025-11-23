package com.example.platemate;

import com.google.gson.annotations.SerializedName;

public class CategoryResponse {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("categoryName")
    private String categoryName;
    
    @SerializedName("description")
    private String description;
    
    // Getters and setters with null safety
    public Long getId() { 
        return id != null ? id : 0L; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public String getCategoryName() { 
        return categoryName != null ? categoryName : ""; 
    }
    
    public void setCategoryName(String categoryName) { 
        this.categoryName = categoryName; 
    }
    
    public String getDescription() { 
        return description != null ? description : ""; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }
}


package com.example.platemate;

public class MenuItemCreateRequest {
    private Long categoryId;
    private String itemName;
    private String description;
    private Double price;
    private String ingredients;
    private String mealType;  // "VEG", "NON_VEG", or "JAIN"
    private Boolean isAvailable;
    
    // Getters and setters
    public Long getCategoryId() { 
        return categoryId; 
    }
    
    public void setCategoryId(Long categoryId) { 
        this.categoryId = categoryId; 
    }
    
    public String getItemName() { 
        return itemName; 
    }
    
    public void setItemName(String itemName) { 
        this.itemName = itemName; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }
    
    public Double getPrice() { 
        return price; 
    }
    
    public void setPrice(Double price) { 
        this.price = price; 
    }
    
    public String getIngredients() { 
        return ingredients; 
    }
    
    public void setIngredients(String ingredients) { 
        this.ingredients = ingredients; 
    }
    
    public String getMealType() { 
        return mealType; 
    }
    
    public void setMealType(String mealType) { 
        this.mealType = mealType; 
    }
    
    public Boolean getIsAvailable() { 
        return isAvailable; 
    }
    
    public void setIsAvailable(Boolean isAvailable) { 
        this.isAvailable = isAvailable; 
    }
}


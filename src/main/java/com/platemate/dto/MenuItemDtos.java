package com.platemate.dto;

import com.platemate.enums.MealType;

public class MenuItemDtos {

    public static class CreateRequest {
        private Long categoryId;
        private String itemName;
        private String description;
        private Double price;
        private String ingredients;
        private MealType mealType;
        private Boolean isAvailable;
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getIngredients() { return ingredients; }
        public void setIngredients(String ingredients) { this.ingredients = ingredients; }
        public MealType getMealType() { return mealType; }
        public void setMealType(MealType mealType) { this.mealType = mealType; }
        public Boolean getIsAvailable() { return isAvailable; }
        public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    }

    public static class UpdateRequest {
        private Long categoryId;
        private String itemName;
        private String description;
        private Double price;
        private String ingredients;
        private MealType mealType;
        private Boolean isAvailable;
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getIngredients() { return ingredients; }
        public void setIngredients(String ingredients) { this.ingredients = ingredients; }
        public MealType getMealType() { return mealType; }
        public void setMealType(MealType mealType) { this.mealType = mealType; }
        public Boolean getIsAvailable() { return isAvailable; }
        public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    }

    public static class Response {
        private Long id;
        private Long categoryId;
        private String itemName;
        private String description;
        private Double price;
        private String ingredients;
        private MealType mealType;
        private Boolean isAvailable;
        private java.util.List<String> imageBase64List;
        private java.util.List<String> imageFileTypeList;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getIngredients() { return ingredients; }
        public void setIngredients(String ingredients) { this.ingredients = ingredients; }
        public MealType getMealType() { return mealType; }
        public void setMealType(MealType mealType) { this.mealType = mealType; }
        public Boolean getIsAvailable() { return isAvailable; }
        public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
        public java.util.List<String> getImageBase64List() { return imageBase64List; }
        public void setImageBase64List(java.util.List<String> imageBase64List) { this.imageBase64List = imageBase64List; }
        public java.util.List<String> getImageFileTypeList() { return imageFileTypeList; }
        public void setImageFileTypeList(java.util.List<String> imageFileTypeList) { this.imageFileTypeList = imageFileTypeList; }
    }

    // Customer-facing DTOs with provider information
    public static class CustomerResponse {
        private Long id;
        private Long categoryId;
        private String categoryName;
        private String itemName;
        private String description;
        private Double price;
        private String ingredients;
        private MealType mealType;
        private Long providerId;
        private String providerName;
        private String providerBusinessName;
        private java.util.List<String> imageBase64List;
        private java.util.List<String> imageFileTypeList;
        
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
        public MealType getMealType() { return mealType; }
        public void setMealType(MealType mealType) { this.mealType = mealType; }
        public Long getProviderId() { return providerId; }
        public void setProviderId(Long providerId) { this.providerId = providerId; }
        public String getProviderName() { return providerName; }
        public void setProviderName(String providerName) { this.providerName = providerName; }
        public String getProviderBusinessName() { return providerBusinessName; }
        public void setProviderBusinessName(String providerBusinessName) { this.providerBusinessName = providerBusinessName; }
        public java.util.List<String> getImageBase64List() { return imageBase64List; }
        public void setImageBase64List(java.util.List<String> imageBase64List) { this.imageBase64List = imageBase64List; }
        public java.util.List<String> getImageFileTypeList() { return imageFileTypeList; }
        public void setImageFileTypeList(java.util.List<String> imageFileTypeList) { this.imageFileTypeList = imageFileTypeList; }
    }

    public static class PaginatedResponse<T> {
        private java.util.List<T> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
        private boolean isFirst;
        private boolean isLast;

        public PaginatedResponse(org.springframework.data.domain.Page<T> page) {
            this.content = page.getContent();
            this.page = page.getNumber();
            this.size = page.getSize();
            this.totalElements = page.getTotalElements();
            this.totalPages = page.getTotalPages();
            this.hasNext = page.hasNext();
            this.hasPrevious = page.hasPrevious();
            this.isFirst = page.isFirst();
            this.isLast = page.isLast();
        }

        public java.util.List<T> getContent() { return content; }
        public void setContent(java.util.List<T> content) { this.content = content; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
        public boolean isHasNext() { return hasNext; }
        public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }
        public boolean isHasPrevious() { return hasPrevious; }
        public void setHasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }
        public boolean isFirst() { return isFirst; }
        public void setFirst(boolean first) { isFirst = first; }
        public boolean isLast() { return isLast; }
        public void setLast(boolean last) { isLast = last; }
    }
}



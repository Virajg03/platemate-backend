package com.example.platemate;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MenuItemResponse {
    @SerializedName("content")
    private List<MenuItem> content;

    @SerializedName("page")
    private Integer page;

    @SerializedName("size")
    private Integer size;

    @SerializedName("totalElements")
    private Long totalElements;

    @SerializedName("totalPages")
    private Integer totalPages;

    @SerializedName("hasNext")
    private Boolean hasNext;

    @SerializedName("hasPrevious")
    private Boolean hasPrevious;

    @SerializedName("isFirst")
    private Boolean isFirst;

    @SerializedName("isLast")
    private Boolean isLast;

    public List<MenuItem> getContent() { return content; }
    public void setContent(List<MenuItem> content) { this.content = content; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }

    public Long getTotalElements() { return totalElements; }
    public void setTotalElements(Long totalElements) { this.totalElements = totalElements; }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }

    public Boolean getHasNext() { return hasNext; }
    public void setHasNext(Boolean hasNext) { this.hasNext = hasNext; }

    public Boolean getHasPrevious() { return hasPrevious; }
    public void setHasPrevious(Boolean hasPrevious) { this.hasPrevious = hasPrevious; }

    public Boolean getIsFirst() { return isFirst; }
    public void setIsFirst(Boolean isFirst) { this.isFirst = isFirst; }

    public Boolean getIsLast() { return isLast; }
    public void setIsLast(Boolean isLast) { this.isLast = isLast; }
}





//package com.example.platemate;
//
//public class MenuItemResponse {
//    private Long id;
//    private Long categoryId;
//    private String itemName;
//    private String description;
//    private Double price;
//    private String ingredients;
//    private String mealType;
//    private Boolean isAvailable;
//
//    // Getters and setters
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public Long getCategoryId() {
//        return categoryId;
//    }
//
//    public void setCategoryId(Long categoryId) {
//        this.categoryId = categoryId;
//    }
//
//    public String getItemName() {
//        return itemName;
//    }
//
//    public void setItemName(String itemName) {
//        this.itemName = itemName;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public Double getPrice() {
//        return price;
//    }
//
//    public void setPrice(Double price) {
//        this.price = price;
//    }
//
//    public String getIngredients() {
//        return ingredients;
//    }
//
//    public void setIngredients(String ingredients) {
//        this.ingredients = ingredients;
//    }
//
//    public String getMealType() {
//        return mealType;
//    }
//
//    public void setMealType(String mealType) {
//        this.mealType = mealType;
//    }
//
//    public Boolean getIsAvailable() {
//        return isAvailable;
//    }
//
//    public void setIsAvailable(Boolean isAvailable) {
//        this.isAvailable = isAvailable;
//    }
//}
//

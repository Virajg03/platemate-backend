package com.platemate.dto;

public class CategoryDtos {

    public static class CreateRequest {
        private String categoryName;
        private String description;
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class UpdateRequest {
        private String categoryName;
        private String description;
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class Response {
        private Long id;
        private String categoryName;
        private String description;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}



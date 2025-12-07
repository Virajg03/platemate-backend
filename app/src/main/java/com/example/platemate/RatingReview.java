package com.example.platemate;

import com.google.gson.annotations.SerializedName;

public class RatingReview {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("rating")
    private Integer rating;
    
    @SerializedName("reviewText")
    private String reviewText;
    
    @SerializedName("customer")
    private CustomerInfo customer;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getRating() {
        return rating;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public String getReviewText() {
        return reviewText;
    }
    
    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }
    
    public CustomerInfo getCustomer() {
        return customer;
    }
    
    public void setCustomer(CustomerInfo customer) {
        this.customer = customer;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public static class CustomerInfo {
        @SerializedName("id")
        private Long id;
        
        @SerializedName("user")
        private UserInfo user;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public UserInfo getUser() {
            return user;
        }
        
        public void setUser(UserInfo user) {
            this.user = user;
        }
        
        public static class UserInfo {
            @SerializedName("username")
            private String username;
            
            public String getUsername() {
                return username;
            }
            
            public void setUsername(String username) {
                this.username = username;
            }
        }
    }
}


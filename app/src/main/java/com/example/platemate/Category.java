package com.example.platemate;

public class Category {
    private String name;
    private int iconResId; // Resource ID for the icon drawable
    private String iconUrl; // Optional: URL for category icon

    public Category() {}

    public Category(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }

    public Category(String name, String iconUrl) {
        this.name = name;
        this.iconUrl = iconUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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


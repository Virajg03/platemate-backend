package com.example.platemate;

import com.google.gson.annotations.SerializedName;

public class Image {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("imageType")
    private String imageType;
    
    @SerializedName("ownerId")
    private Long ownerId;
    
    @SerializedName("fileType")
    private String fileType;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getImageType() { return imageType; }
    public void setImageType(String imageType) { this.imageType = imageType; }
    
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
}


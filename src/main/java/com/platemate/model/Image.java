package com.platemate.model;

import com.platemate.enums.ImageType;

import jakarta.persistence.*;

@Entity
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String fileType;

    @Lob
    private String base64Data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageType imageType;  // PROFILE / PRODUCT / PLACE

    // this helps us link image to any entity
    private Long ownerId;

    public Image() {
    }

    public Image(String fileName, String fileType, String base64Data, ImageType imageType, Long ownerId) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.base64Data = base64Data;
        this.imageType = imageType;
        this.ownerId = ownerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getBase64Data() {
        return base64Data;
    }

    public void setBase64Data(String base64Data) {
        this.base64Data = base64Data;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    
}
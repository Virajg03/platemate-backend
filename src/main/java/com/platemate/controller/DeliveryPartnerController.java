package com.platemate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.platemate.enums.ImageType;
import com.platemate.service.ImageService;

@RestController
@RequestMapping("/api/delivery-partners")
public class DeliveryPartnerController {

    @Autowired
    private ImageService imageService;

    @PostMapping(path = "/{id}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','DELIVERY_PARTNER')")
    public ResponseEntity<com.platemate.model.Image> uploadDeliveryPartnerProfileImage(@PathVariable Long id, @RequestPart("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(imageService.saveImage(file, ImageType.DELIVERY_PARTNER_PROFILE, id));
    }
}



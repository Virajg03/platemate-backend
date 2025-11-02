package com.platemate.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.platemate.dto.TiffinProviderRequest;
import com.platemate.model.TiffinProvider;
import com.platemate.service.ImageService;
import com.platemate.service.TiffinProviderService;
import com.platemate.utils.ImageResponse;

import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/tiffin-providers")
public class TiffinProviderController {

    @Autowired
    private TiffinProviderService service;

    @Autowired
    private ImageService imageService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    public ResponseEntity<List<TiffinProvider>> getAllProviders() {
        return ResponseEntity.ok(service.getAllProviders());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    public ResponseEntity<TiffinProvider> getProviderById(@PathVariable Long id) {
        return service.getProviderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/PROVIDER_PROFILE")
    public ResponseEntity<byte[]> getProviderProfileImage(@PathVariable Long id) {
        
        ImageResponse imageResponse = imageService.getProviderProfileImage(id);
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(imageResponse.getFileType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(imageResponse.getData());
    }
    

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    public ResponseEntity<TiffinProvider> createProvider(@RequestBody TiffinProviderRequest request) {
        return ResponseEntity.ok(service.createProvider(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    public ResponseEntity<TiffinProvider> updateProvider(@PathVariable Long id, @RequestBody TiffinProvider provider) {
        try {
            return ResponseEntity.ok(service.updateProvider(id, provider));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    public ResponseEntity<Void> deleteProvider(@PathVariable Long id) {
        service.deleteProvider(id);
        return ResponseEntity.noContent().build();
    }
}


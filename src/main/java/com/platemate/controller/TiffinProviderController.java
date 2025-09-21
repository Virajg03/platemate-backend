package com.platemate.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.platemate.model.TiffinProvider;
import com.platemate.service.TiffinProviderService;

@RestController
@RequestMapping("/api/tiffin-providers")
public class TiffinProviderController {

    @Autowired
    private TiffinProviderService service;

    @GetMapping
    public ResponseEntity<List<TiffinProvider>> getAllProviders() {
        return ResponseEntity.ok(service.getAllProviders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TiffinProvider> getProviderById(@PathVariable Long id) {
        return service.getProviderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN', 'ROLE_PROVIDER')")
    public ResponseEntity<TiffinProvider> createProvider(@RequestBody TiffinProvider provider) {
        return ResponseEntity.ok(service.createProvider(provider));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN', 'ROLE_PROVIDER')")
    public ResponseEntity<TiffinProvider> updateProvider(@PathVariable Long id, @RequestBody TiffinProvider provider) {
        try {
            return ResponseEntity.ok(service.updateProvider(id, provider));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN', 'ROLE_PROVIDER')")
    public ResponseEntity<Void> deleteProvider(@PathVariable Long id) {
        service.deleteProvider(id);
        return ResponseEntity.noContent().build();
    }
}


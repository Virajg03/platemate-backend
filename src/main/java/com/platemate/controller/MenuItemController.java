package com.platemate.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.platemate.exception.ForbiddenException;
import com.platemate.exception.ResourceNotFoundException;
import com.platemate.model.MenuItem;
import com.platemate.model.TiffinProvider;
import com.platemate.model.User;
import com.platemate.repository.MenuItemRepository;
import com.platemate.repository.TiffinProviderRepository;
import com.platemate.repository.UserRepository;

@RestController
@RequestMapping("/api/providers/menu-items")
@PreAuthorize("hasRole('PROVIDER')")
public class MenuItemController {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private TiffinProviderRepository providerRepository;

    @Autowired
    private UserRepository userRepository;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private TiffinProvider currentProviderOrThrow() {
        User user = currentUser();
        TiffinProvider provider = providerRepository.findByUser_Id(user.getId());
        if (provider == null) {
            throw new ResourceNotFoundException("Provider profile not found for user");
        }
        if (!Boolean.TRUE.equals(provider.getIsVerified())) {
            throw new ForbiddenException("Provider is not approved yet");
        }
        return provider;
    }

    @PostMapping
    public ResponseEntity<MenuItem> create(@RequestBody MenuItem body) {
        TiffinProvider provider = currentProviderOrThrow();
        body.setProvider(provider);
        body.setIsDeleted(false);
        return ResponseEntity.ok(menuItemRepository.save(body));
    }

    @GetMapping
    public ResponseEntity<List<MenuItem>> listMine() {
        TiffinProvider provider = currentProviderOrThrow();
        List<MenuItem> items = menuItemRepository.findAll().stream()
                .filter(mi -> mi.getProvider().getId().equals(provider.getId()))
                .filter(mi -> !Boolean.TRUE.equals(mi.getIsDeleted()))
                .toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItem> getMine(@PathVariable Long id) {
        TiffinProvider provider = currentProviderOrThrow();
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        if (!item.getProvider().getId().equals(provider.getId())) {
            throw new ForbiddenException("Cannot access another provider's item");
        }
        return ResponseEntity.ok(item);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItem> update(@PathVariable Long id, @RequestBody MenuItem body) {
        TiffinProvider provider = currentProviderOrThrow();
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        if (!item.getProvider().getId().equals(provider.getId())) {
            throw new ForbiddenException("Cannot modify another provider's item");
        }
        item.setCategory(body.getCategory());
        item.setDescription(body.getDescription());
        item.setItemName(body.getItemName());
        item.setIngredients(body.getIngredients());
        item.setMealType(body.getMealType());
        item.setPrice(body.getPrice());
        item.setIsAvailable(body.getIsAvailable() != null ? body.getIsAvailable() : item.getIsAvailable());
        return ResponseEntity.ok(menuItemRepository.save(item));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<MenuItem> toggleAvailability(@PathVariable Long id, @RequestBody java.util.Map<String, Boolean> req) {
        TiffinProvider provider = currentProviderOrThrow();
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        if (!item.getProvider().getId().equals(provider.getId())) {
            throw new ForbiddenException("Cannot modify another provider's item");
        }
        Boolean available = req.get("available");
        if (available != null) {
            item.setIsAvailable(available);
        }
        return ResponseEntity.ok(menuItemRepository.save(item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        TiffinProvider provider = currentProviderOrThrow();
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        if (!item.getProvider().getId().equals(provider.getId())) {
            throw new ForbiddenException("Cannot modify another provider's item");
        }
        item.setIsDeleted(true);
        menuItemRepository.save(item);
        return ResponseEntity.noContent().build();
    }
}



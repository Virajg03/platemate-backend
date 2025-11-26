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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.platemate.dto.DeliveryPartnerDtos;
import com.platemate.exception.ResourceNotFoundException;
import com.platemate.model.DeliveryPartner;
import com.platemate.model.TiffinProvider;
import com.platemate.model.User;
import com.platemate.repository.TiffinProviderRepository;
import com.platemate.repository.UserRepository;
import com.platemate.service.DeliveryPartnerService;

@RestController
@RequestMapping("/api/providers/delivery-partners")
@PreAuthorize("hasRole('PROVIDER')")
public class ProviderDeliveryPartnerController {

    @Autowired
    private DeliveryPartnerService service;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TiffinProviderRepository tiffinProviderRepository;

    /**
     * List all delivery partners owned by the current provider.
     * Returns all delivery partners created by this provider (can be multiple).
     * 
     * @return List of delivery partners owned by the provider
     */
    @GetMapping
    public ResponseEntity<List<DeliveryPartnerDtos.Response>> list() {
        TiffinProvider provider = getCurrentProvider();
        // Returns all delivery partners for this provider (supports multiple)
        List<DeliveryPartner> partners = service.listByProvider(provider.getId());
        List<DeliveryPartnerDtos.Response> data = partners.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/available")
    public ResponseEntity<List<DeliveryPartnerDtos.Response>> listAvailable() {
        TiffinProvider provider = getCurrentProvider();
        List<DeliveryPartner> partners = service.listForProvider(provider.getId());
        List<DeliveryPartnerDtos.Response> data = partners.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsAvailable()))
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(data);
    }

    /**
     * Create a new delivery partner for the current provider.
     * A provider can create multiple delivery partners (no limit).
     * Each delivery partner must have a unique user.
     * 
     * @param req The delivery partner creation request
     * @return The created delivery partner
     */
    @PostMapping
    public ResponseEntity<DeliveryPartnerDtos.Response> create(@RequestBody DeliveryPartnerDtos.CreateRequest req) {
        TiffinProvider provider = getCurrentProvider();
        User user = service.requireUser(req.getUserId());
        
        DeliveryPartner p = new DeliveryPartner();
        p.setUser(user);
        p.setFullName(req.getFullName());
        p.setVehicleType(req.getVehicleType());
        p.setCommissionRate(req.getCommissionRate());
        p.setServiceArea(req.getServiceArea());
        
        // Provider can create multiple delivery partners - no limit
        DeliveryPartner saved = service.create(p, provider.getId());
        return ResponseEntity.ok(toResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryPartnerDtos.Response> getById(@PathVariable Long id) {
        TiffinProvider provider = getCurrentProvider();
        DeliveryPartner partner = service.getByIdAndProvider(id, provider.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found with id " + id));
        return ResponseEntity.ok(toResponse(partner));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliveryPartnerDtos.Response> update(
            @PathVariable Long id,
            @RequestBody DeliveryPartnerDtos.UpdateRequest req) {
        TiffinProvider provider = getCurrentProvider();
        
        DeliveryPartner u = new DeliveryPartner();
        u.setFullName(req.getFullName());
        u.setVehicleType(req.getVehicleType());
        u.setCommissionRate(req.getCommissionRate());
        u.setServiceArea(req.getServiceArea());
        u.setIsAvailable(req.getIsAvailable());
        
        DeliveryPartner saved = service.update(id, u, provider.getId());
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        TiffinProvider provider = getCurrentProvider();
        service.softDelete(id, provider.getId());
        return ResponseEntity.noContent().build();
    }

    private DeliveryPartnerDtos.Response toResponse(DeliveryPartner p) {
        DeliveryPartnerDtos.Response res = new DeliveryPartnerDtos.Response();
        res.setId(p.getId());
        res.setUserId(p.getUser() != null ? p.getUser().getId() : null);
        res.setProviderId(p.getProviderId());
        res.setFullName(p.getFullName());
        res.setVehicleType(p.getVehicleType());
        res.setCommissionRate(p.getCommissionRate());
        res.setServiceArea(p.getServiceArea());
        res.setIsAvailable(p.getIsAvailable());
        return res;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private TiffinProvider getCurrentProvider() {
        User user = getCurrentUser();
        TiffinProvider provider = tiffinProviderRepository.findByUser_Id(user.getId());
        if (provider == null) {
            throw new ResourceNotFoundException("Provider profile not found for user");
        }
        return provider;
    }
}


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

import com.platemate.dto.DeliveryPartnerDtos;
import com.platemate.model.DeliveryPartner;
import com.platemate.model.User;
import com.platemate.service.DeliveryPartnerService;

@RestController
@RequestMapping("/api/delivery-partners")
public class DeliveryPartnerCrudController {

    @Autowired
    private DeliveryPartnerService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DELIVERY_PARTNER')")
    public ResponseEntity<DeliveryPartnerDtos.Response> create(@RequestBody DeliveryPartnerDtos.CreateRequest req) {
        User user = service.requireUser(req.getUserId());
        DeliveryPartner p = new DeliveryPartner();
        p.setUser(user);
        p.setFullName(req.getFullName());
        p.setVehicleType(req.getVehicleType());
        p.setCommissionRate(req.getCommissionRate());
        p.setServiceArea(req.getServiceArea());
        DeliveryPartner saved = service.create(p);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DELIVERY_PARTNER')")
    public ResponseEntity<DeliveryPartnerDtos.Response> update(@PathVariable Long id, @RequestBody DeliveryPartnerDtos.UpdateRequest req) {
        DeliveryPartner u = new DeliveryPartner();
        u.setFullName(req.getFullName());
        u.setVehicleType(req.getVehicleType());
        u.setCommissionRate(req.getCommissionRate());
        u.setServiceArea(req.getServiceArea());
        u.setIsAvailable(req.getIsAvailable());
        DeliveryPartner saved = service.update(id, u);
        return ResponseEntity.ok(toResponse(saved));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','DELIVERY_PARTNER')")
    public ResponseEntity<List<DeliveryPartnerDtos.Response>> list() {
        List<DeliveryPartnerDtos.Response> data = service.listActive().stream().map(this::toResponse).toList();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','DELIVERY_PARTNER')")
    public ResponseEntity<DeliveryPartnerDtos.Response> getById(@PathVariable Long id) {
        return service.getById(id).map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    private DeliveryPartnerDtos.Response toResponse(DeliveryPartner p) {
        DeliveryPartnerDtos.Response res = new DeliveryPartnerDtos.Response();
        res.setId(p.getId());
        res.setUserId(p.getUser() != null ? p.getUser().getId() : null);
        res.setFullName(p.getFullName());
        res.setVehicleType(p.getVehicleType());
        res.setCommissionRate(p.getCommissionRate());
        res.setServiceArea(p.getServiceArea());
        res.setIsAvailable(p.getIsAvailable());
        return res;
    }
}



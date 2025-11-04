package com.platemate.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.platemate.exception.ResourceNotFoundException;
import com.platemate.model.DeliveryPartner;
import com.platemate.model.User;
import com.platemate.repository.DeliveryPartnerRepository;
import com.platemate.repository.UserRepository;

@Service
public class DeliveryPartnerService {

    @Autowired
    private DeliveryPartnerRepository repository;
    @Autowired
    private UserRepository userRepository;

    public DeliveryPartner create(DeliveryPartner partner) {
        return repository.save(partner);
    }

    public List<DeliveryPartner> listActive() {
        return repository.findAllByIsDeletedFalse();
    }

    public Optional<DeliveryPartner> getById(Long id) {
        return repository.findById(id).filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()));
    }

    public DeliveryPartner update(Long id, DeliveryPartner update) {
        return repository.findById(id).map(p -> {
            if (update.getFullName() != null) p.setFullName(update.getFullName());
            if (update.getVehicleType() != null) p.setVehicleType(update.getVehicleType());
            if (update.getCommissionRate() != null) p.setCommissionRate(update.getCommissionRate());
            if (update.getServiceArea() != null) p.setServiceArea(update.getServiceArea());
            if (update.getIsAvailable() != null) p.setIsAvailable(update.getIsAvailable());
            return repository.save(p);
        }).orElseThrow(() -> new ResourceNotFoundException("DeliveryPartner not found with id " + id));
    }

    public void softDelete(Long id) {
        DeliveryPartner p = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPartner not found with id " + id));
        p.setIsDeleted(true);
        repository.save(p);
    }

    public User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
    }
}



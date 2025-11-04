package com.platemate.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.platemate.model.DeliveryPartner;

public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {
    List<DeliveryPartner> findAllByIsDeletedFalse();
    
    Optional<DeliveryPartner> findByUser_IdAndIsDeletedFalse(Long userId);
}



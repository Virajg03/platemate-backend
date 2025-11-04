package com.platemate.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.platemate.model.MenuItem;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    java.util.List<MenuItem> findAllByProvider_IdAndIsDeletedFalse(Long providerId);
}

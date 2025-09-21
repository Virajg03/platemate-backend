package com.platemate.repository;

import com.platemate.model.TiffinProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TiffinProviderRepository extends JpaRepository<TiffinProvider, Long> {
    // Custom query if needed, e.g., find by userId
    TiffinProvider findByUser_UserId(Long userId);
}


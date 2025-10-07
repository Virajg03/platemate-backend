package com.platemate.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.platemate.enums.ImageType;
import com.platemate.model.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByImageTypeAndOwnerId(ImageType profile, Long ownerId);
}
package com.platemate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.platemate.enums.ImageType;
import com.platemate.model.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {


    @Query("SELECT i.id FROM Image i WHERE i.imageType = :imageType AND i.ownerId = :ownerId")
    Long findIdByImageTypeAndOwnerId(@Param("imageType") ImageType imageType, @Param("ownerId") Long ownerId);
}
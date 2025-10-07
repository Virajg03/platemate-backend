package com.platemate.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.platemate.enums.RatingType;
import com.platemate.model.RatingReview;

public interface RatingReviewRepository extends JpaRepository<RatingReview, Long> {
    List<RatingReview> findByRatingTypeAndTargetId(RatingType ratingType, Long targetId);
}
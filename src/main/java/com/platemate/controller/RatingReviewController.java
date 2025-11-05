package com.platemate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.platemate.enums.RatingType;
import com.platemate.exception.ResourceNotFoundException;
import com.platemate.model.User;
import com.platemate.repository.UserRepository;
import com.platemate.service.RatingReviewService;

@RestController
@RequestMapping("/api")
public class RatingReviewController {

    @Autowired
    private RatingReviewService service;

    @Autowired
    private UserRepository userRepository;

    public static class RateProviderRequest {
        public Long orderId;
        public Long providerId;
        public Integer rating;
        public String review;
    }

    public static class SummaryRequest {
        public RatingType type;
        public Long targetId;
    }

    @PostMapping("/customers/ratings/provider")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> rateProvider(@RequestBody RateProviderRequest req) {
        Long customerId = getCurrentUserId();
        return ResponseEntity.ok(service.rateProvider(customerId, req.orderId, req.providerId, req.rating, req.review));
    }

    @GetMapping("/ratings/summary")
    public ResponseEntity<?> summary(@RequestBody SummaryRequest req) {
        return ResponseEntity.ok(service.getSummary(req.type, req.targetId));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return user.getId();
    }
}



package com.platemate.service;

import java.util.DoubleSummaryStatistics;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.platemate.enums.OrderStatus;
import com.platemate.enums.RatingType;
import com.platemate.exception.BadRequestException;
import com.platemate.exception.ResourceNotFoundException;
import com.platemate.model.Customer;
import com.platemate.model.Order;
import com.platemate.model.RatingReview;
import com.platemate.repository.OrderRepository;
import com.platemate.repository.RatingReviewRepository;

@Service
public class RatingReviewService {

    @Autowired
    private RatingReviewRepository ratingRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public RatingReview rateProvider(Long customerId, Long orderId, Long providerId, Integer rating, String review) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException("Order not found");
        }
        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("You can rate only after delivery");
        }
        if (!order.getProvider().getId().equals(providerId)) {
            throw new BadRequestException("Order does not belong to this provider");
        }

        RatingReview rr = new RatingReview();
        rr.setCustomer(order.getCustomer());
        rr.setRatingType(RatingType.COOK_RATING);
        rr.setTargetId(providerId);
        rr.setRating(rating);
        rr.setReviewText(review);
        return ratingRepository.save(rr);
    }

    @Transactional(readOnly = true)
    public Summary getSummary(RatingType type, Long targetId) {
        List<RatingReview> list = ratingRepository.findByRatingTypeAndTargetId(type, targetId);
        if (list.isEmpty()) return new Summary(0L, 0.0);
        DoubleSummaryStatistics stats = list.stream().mapToDouble(r -> r.getRating()).summaryStatistics();
        double avg = Math.round(stats.getAverage() * 10.0) / 10.0;
        return new Summary(stats.getCount(), avg);
    }

    public static class Summary {
        private long count;
        private double average;
        public Summary(long count, double average) { this.count = count; this.average = average; }
        public long getCount() { return count; }
        public double getAverage() { return average; }
    }
}



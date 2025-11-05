package com.platemate.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.platemate.dto.PaymentDtos;
import com.platemate.exception.ResourceNotFoundException;
import com.platemate.model.Customer;
import com.platemate.model.Order;
import com.platemate.model.User;
import com.platemate.repository.CustomerRepository;
import com.platemate.repository.OrderRepository;
import com.platemate.repository.UserRepository;
import com.platemate.service.PaymentService;

@RestController
@RequestMapping("/api")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("/customers/payments/orders/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentDtos.CreateOrderResponse> createPaymentOrder(@PathVariable Long orderId) {
        Customer customer = getCurrentCustomer();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new ResourceNotFoundException("Order not found with id " + orderId);
        }

        Map<String, Object> data = paymentService.createRazorpayOrder(orderId);
        PaymentDtos.CreateOrderResponse res = new PaymentDtos.CreateOrderResponse();
        res.setRazorpayOrderId(String.valueOf(data.get("razorpayOrderId")));
        Object amt = data.get("amount");
        Long amount = amt instanceof Number ? ((Number) amt).longValue() : Long.parseLong(String.valueOf(amt));
        res.setAmount(amount);
        res.setCurrency(String.valueOf(data.get("currency")));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/customers/payments/orders/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentDtos.CreateOrderResponse> getPaymentOrder(@PathVariable Long orderId) {
        // For simplicity, reissue the data by creating/fetching the Razorpay order again
        return createPaymentOrder(orderId);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private Customer getCurrentCustomer() {
        User user = getCurrentUser();
        return customerRepository.findByUser_IdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for user"));
    }
}



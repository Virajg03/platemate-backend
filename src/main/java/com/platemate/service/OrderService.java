package com.platemate.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platemate.dto.OrderDtos;
import com.platemate.enums.OrderStatus;
import com.platemate.exception.BadRequestException;
import com.platemate.exception.ForbiddenException;
import com.platemate.exception.ResourceNotFoundException;
import com.platemate.model.Cart;
import com.platemate.model.Customer;
import com.platemate.model.DeliveryPartner;
import com.platemate.model.Order;
import com.platemate.model.TiffinProvider;
import com.platemate.repository.CartRepository;
import com.platemate.repository.CustomerRepository;
import com.platemate.repository.DeliveryPartnerRepository;
import com.platemate.repository.OrderRepository;
import com.platemate.repository.TiffinProviderRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TiffinProviderRepository tiffinProviderRepository;

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Autowired
    private CartService cartService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public Order createOrder(Long customerId, OrderDtos.CreateRequest req) {
        // Validate customer exists
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + customerId));

        // Validate delivery address
        if (req.getDeliveryAddress() == null || req.getDeliveryAddress().trim().isEmpty()) {
            throw new BadRequestException("Delivery address is required");
        }

        // Validate cart items
        if (req.getCartItemIds() == null || req.getCartItemIds().isEmpty()) {
            throw new BadRequestException("Cart item IDs cannot be empty");
        }

        List<Cart> cartItems = cartService.validateCartItems(customerId, req.getCartItemIds());

        // Validate all items from same provider
        Long providerId = cartItems.get(0).getMenuItem().getProvider().getId();
        for (Cart cart : cartItems) {
            if (!cart.getMenuItem().getProvider().getId().equals(providerId)) {
                throw new BadRequestException("All cart items must be from the same provider");
            }
        }

        // Get provider
        TiffinProvider provider = tiffinProviderRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with id " + providerId));

        if (Boolean.TRUE.equals(provider.getIsDeleted())) {
            throw new BadRequestException("Provider has been deleted");
        }

        if (!Boolean.TRUE.equals(provider.getIsVerified())) {
            throw new BadRequestException("Provider is not verified");
        }

        // Calculate subtotal
        Double subtotal = cartItems.stream()
                .mapToDouble(Cart::getItemTotal)
                .sum();

        // Validate minimum order amount (if needed)
        if (subtotal <= 0) {
            throw new BadRequestException("Order subtotal must be greater than 0");
        }

        // Calculate delivery fee (fixed 50.0 for now)
        Double deliveryFee = req.getDeliveryFee() != null && req.getDeliveryFee() >= 0 ? req.getDeliveryFee() : 50.0;

        // Calculate platform commission (percentage of subtotal)
        Double commissionRate = provider.getCommissionRate() != null ? provider.getCommissionRate() : 0.0;
        Double platformCommission = Math.round((subtotal * commissionRate) / 100.0 * 100.0) / 100.0; // Round to 2 decimal places

        // Calculate total amount (round to 2 decimal places)
        Double totalAmount = Math.round((subtotal + deliveryFee + platformCommission) * 100.0) / 100.0;

        // Create order
        Order order = new Order();
        order.setCustomer(customer);
        order.setProvider(provider);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setDeliveryFee(deliveryFee);
        order.setPlatformCommission(platformCommission);
        order.setTotalAmount(totalAmount);
        order.setDeliveryAddress(req.getDeliveryAddress());
        order.setIsDeleted(false);

        // Store cart item IDs as JSON
        try {
            List<Long> cartItemIds = cartItems.stream()
                    .map(Cart::getId)
                    .collect(Collectors.toList());
            String cartItemIdsJson = objectMapper.writeValueAsString(cartItemIds);
            order.setCartItemIds(cartItemIdsJson);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Failed to serialize cart item IDs");
        }

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Soft delete cart items after order creation
        cartItems.forEach(cart -> cart.setIsDeleted(true));
        cartRepository.saveAll(cartItems);

        return savedOrder;
    }

    public Order getOrderById(Long orderId, Long customerId) {
        Optional<Order> order = orderRepository.findByIdAndCustomer_IdAndIsDeletedFalse(orderId, customerId);
        return order.orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));
    }

    public List<Order> getCustomerOrders(Long customerId) {
        return orderRepository.findAllByCustomer_IdAndIsDeletedFalseOrderByOrderTimeDesc(customerId);
    }

    public Order getProviderOrderById(Long orderId, Long providerId) {
        Optional<Order> order = orderRepository.findByIdAndProvider_IdAndIsDeletedFalse(orderId, providerId);
        return order.orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));
    }

    public List<Order> getProviderOrders(Long providerId) {
        return orderRepository.findAllByProvider_IdAndIsDeletedFalseOrderByOrderTimeDesc(providerId);
    }

    public Order getDeliveryPartnerOrderById(Long orderId, Long deliveryPartnerId) {
        Optional<Order> order = orderRepository.findByIdAndDeliveryPartner_IdAndIsDeletedFalse(orderId, deliveryPartnerId);
        return order.orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));
    }

    public List<Order> getDeliveryPartnerOrders(Long deliveryPartnerId) {
        return orderRepository.findAllByDeliveryPartner_IdAndIsDeletedFalseOrderByOrderTimeDesc(deliveryPartnerId);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, Long providerId, OrderDtos.UpdateStatusRequest req) {
        Order order = getProviderOrderById(orderId, providerId);

        OrderStatus newStatus = req.getOrderStatus();
        OrderStatus currentStatus = order.getOrderStatus();

        // Validate status transition
        if (newStatus == null) {
            throw new BadRequestException("Order status cannot be null");
        }
        
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new BadRequestException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        order.setOrderStatus(newStatus);

        // Set estimated delivery time if provided
        if (req.getEstimatedDeliveryTime() != null) {
            order.setEstimatedDeliveryTime(req.getEstimatedDeliveryTime());
        }

        // Set delivery time when status is DELIVERED
        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveryTime(LocalDateTime.now());
        }

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateDeliveryStatus(Long orderId, Long deliveryPartnerId, OrderDtos.UpdateStatusRequest req) {
        Order order = getDeliveryPartnerOrderById(orderId, deliveryPartnerId);

        OrderStatus newStatus = req.getOrderStatus();
        OrderStatus currentStatus = order.getOrderStatus();

        // Delivery partner can only update to OUT_FOR_DELIVERY or DELIVERED
        if (newStatus != OrderStatus.OUT_FOR_DELIVERY && newStatus != OrderStatus.DELIVERED) {
            throw new BadRequestException("Delivery partner can only update status to OUT_FOR_DELIVERY or DELIVERED");
        }

        // Validate status transition
        if (currentStatus == OrderStatus.READY && newStatus == OrderStatus.OUT_FOR_DELIVERY) {
            order.setOrderStatus(newStatus);
        } else if (currentStatus == OrderStatus.OUT_FOR_DELIVERY && newStatus == OrderStatus.DELIVERED) {
            order.setOrderStatus(newStatus);
            order.setDeliveryTime(LocalDateTime.now());
        } else {
            throw new BadRequestException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        return orderRepository.save(order);
    }

    @Transactional
    public Order assignDeliveryPartner(Long orderId, Long deliveryPartnerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));

        if (Boolean.TRUE.equals(order.getIsDeleted())) {
            throw new ResourceNotFoundException("Order not found with id " + orderId);
        }

        // Validate order is ready for delivery
        if (order.getOrderStatus() != OrderStatus.READY) {
            throw new BadRequestException("Order must be in READY status to assign delivery partner");
        }

        // Validate delivery partner exists and is available
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findById(deliveryPartnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found with id " + deliveryPartnerId));

        if (Boolean.TRUE.equals(deliveryPartner.getIsDeleted())) {
            throw new BadRequestException("Delivery partner has been deleted");
        }

        if (!Boolean.TRUE.equals(deliveryPartner.getIsAvailable())) {
            throw new BadRequestException("Delivery partner is not available");
        }

        // Optional: Validate delivery partner zone matches provider zone (if zone-based delivery)
        // This can be implemented later based on business requirements

        order.setDeliveryPartner(deliveryPartner);
        order.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);

        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId, Long customerId) {
        Order order = getOrderById(orderId, customerId);

        // Only allow cancellation if order is PENDING or CONFIRMED
        if (order.getOrderStatus() != OrderStatus.PENDING && order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Order can only be cancelled if it is PENDING or CONFIRMED");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll().stream()
                .filter(order -> !Boolean.TRUE.equals(order.getIsDeleted()))
                .collect(Collectors.toList());
    }

    private boolean isValidStatusTransition(OrderStatus current, OrderStatus next) {
        // Allow cancellation from any status (handled separately)
        if (next == OrderStatus.CANCELLED) {
            return true;
        }

        // Valid transitions
        switch (current) {
            case PENDING:
                return next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED:
                return next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
            case PREPARING:
                return next == OrderStatus.READY || next == OrderStatus.CANCELLED;
            case READY:
                return next == OrderStatus.OUT_FOR_DELIVERY || next == OrderStatus.CANCELLED;
            case OUT_FOR_DELIVERY:
                return next == OrderStatus.DELIVERED;
            case DELIVERED:
            case CANCELLED:
                return false; // Terminal states
            default:
                return false;
        }
    }

    public List<Cart> getOrderCartItems(Order order) {
        if (order == null || order.getCartItemIds() == null || order.getCartItemIds().trim().isEmpty()) {
            return List.of();
        }
        
        try {
            List<Long> cartItemIds = objectMapper.readValue(order.getCartItemIds(), new TypeReference<List<Long>>() {});
            if (cartItemIds.isEmpty()) {
                return List.of();
            }
            // Note: Cart items may be soft deleted after order creation, but we still return them for order history
            // This allows viewing order details even after cart items are cleared
            List<Cart> cartItems = cartRepository.findAllByIdInAndIsDeletedFalse(cartItemIds);
            // If some items are missing (soft deleted), we still return what we have for order history
            return cartItems;
        } catch (JsonProcessingException e) {
            // Log error but return empty list to prevent breaking order view
            // In production, you might want to log this error
            return List.of();
        }
    }
}


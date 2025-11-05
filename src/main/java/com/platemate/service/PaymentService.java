package com.platemate.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.platemate.config.RazorpayProperties;
import com.platemate.enums.OrderStatus;
import com.platemate.enums.PaymentMethod;
import com.platemate.enums.PaymentStatus;
import com.platemate.enums.PaymentType;
import com.platemate.exception.BadRequestException;
import com.platemate.exception.ResourceNotFoundException;
import com.platemate.model.Order;
import com.platemate.model.Payment;
import com.platemate.repository.OrderRepository;
import com.platemate.repository.PaymentRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class PaymentService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RestTemplate razorpayRestTemplate;

    @Autowired
    private RazorpayProperties props;

    private static final String RAZORPAY_API_BASE = "https://api.razorpay.com/v1";

    public Map<String, Object> createRazorpayOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));

        if (Boolean.TRUE.equals(order.getIsDeleted())) {
            throw new BadRequestException("Order is deleted");
        }

        double amount = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
        if (amount <= 0.0) {
            throw new BadRequestException("Invalid order amount");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", (long) Math.round(amount * 100)); // paise
        payload.put("currency", props.getRazorpay().getCurrency());
        payload.put("receipt", "order_" + order.getId());
        payload.put("payment_capture", 1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> rpOrder = razorpayRestTemplate.postForObject(
                RAZORPAY_API_BASE + "/orders", req, Map.class);

        if (rpOrder == null || rpOrder.get("id") == null) {
            throw new BadRequestException("Failed to create Razorpay order");
        }

        // Create a pending Payment record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentType(PaymentType.PREPAID);
        payment.setAmount(amount);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(PaymentMethod.UPI); // actual method finalized after success
        payment.setTransactionId(String.valueOf(rpOrder.get("id")));
        payment.setPaymentTime(LocalDateTime.now());
        payment.setIsDeleted(false);
        paymentRepository.save(payment);

        Map<String, Object> response = new HashMap<>();
        response.put("razorpayOrderId", rpOrder.get("id"));
        response.put("amount", rpOrder.get("amount"));
        response.put("currency", rpOrder.get("currency"));
        return response;
    }

    public Payment markSuccess(String razorpayPaymentId, String razorpayOrderId) {
        Payment payment = paymentRepository.findByTransactionId(razorpayOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order " + razorpayOrderId));
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(razorpayPaymentId);
        payment.setPaymentTime(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        // Update order status to CONFIRMED if currently PENDING
        Order order = saved.getOrder();
        if (order != null && order.getOrderStatus() == OrderStatus.PENDING) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }
        return saved;
    }

    public Payment markFailed(String razorpayOrderId) {
        Payment payment = paymentRepository.findByTransactionId(razorpayOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order " + razorpayOrderId));
        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setPaymentTime(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public void verifyAndProcessWebhook(String payload, String signature) {
        String secret = props.getRazorpay().getWebhookSecret();
        if (!verifySignature(payload, signature, secret)) {
            throw new BadRequestException("Invalid webhook signature");
        }

        // Minimal parsing to detect payment status; in production, use a typed model
        // and handle multiple event types like payment.captured, payment.failed
        String paymentId = extractJsonValue(payload, "payment_id");
        String orderId = extractJsonValue(payload, "order_id");
        String event = extractJsonValue(payload, "event");

        if (event != null && event.contains("payment.captured")) {
            if (paymentId != null && orderId != null) {
                markSuccess(paymentId, orderId);
            }
        } else if (event != null && event.contains("payment.failed")) {
            if (orderId != null) {
                markFailed(orderId);
            }
        }
    }

    private boolean verifySignature(String payload, String expectedSignature, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String actual = toHex(hash);
            return actual.equals(expectedSignature);
        } catch (Exception e) {
            return false;
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String extractJsonValue(String json, String key) {
        // very basic extractor; replace with proper JSON parsing if needed
        String needle = "\"" + key + "\":";
        int idx = json.indexOf(needle);
        if (idx < 0) return null;
        int start = json.indexOf('"', idx + needle.length());
        if (start < 0) return null;
        int end = json.indexOf('"', start + 1);
        if (end < 0) return null;
        return json.substring(start + 1, end);
    }
}



package com.platemate.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.platemate.config.RazorpayProperties;
import com.platemate.dto.PayoutDtos;
import com.platemate.enums.OrderStatus;
import com.platemate.enums.PayoutStatus;
import com.platemate.enums.RecipientType;
import com.platemate.exception.BadRequestException;
import com.platemate.model.Order;
import com.platemate.model.Payout;
import com.platemate.repository.OrderRepository;
import com.platemate.repository.PayoutRepository;

@Service
public class PayoutService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PayoutRepository payoutRepository;

    @Autowired
    private RestTemplate razorpayXRestTemplate;

    @Autowired
    private RazorpayProperties props;

    private static final String RAZORPAYX_API_BASE = "https://api.razorpay.com/v1"; // RazorpayX shares domain

    public PayoutDtos.StatementResponse computeEarningsForProvider(Long providerId, LocalDateTime from, LocalDateTime to) {
        List<Order> orders = orderRepository.findAllByProvider_IdAndIsDeletedFalseOrderByOrderTimeDesc(providerId);
        double gross = 0.0;
        double commission = 0.0;
        for (Order o : orders) {
            LocalDateTime t = o.getOrderTime();
            if (o.getOrderStatus() == OrderStatus.DELIVERED &&
                (from == null || !t.isBefore(from)) && (to == null || !t.isAfter(to))) {
                double total = o.getTotalAmount() != null ? o.getTotalAmount() : 0.0;
                double comm = o.getPlatformCommission() != null ? o.getPlatformCommission() : 0.0;
                gross += total;
                commission += comm;
            }
        }
        PayoutDtos.StatementResponse res = new PayoutDtos.StatementResponse();
        res.setGross(Math.round(gross * 100.0) / 100.0);
        res.setPlatformCommission(Math.round(commission * 100.0) / 100.0);
        res.setNetPayable(Math.round((gross - commission) * 100.0) / 100.0);
        return res;
    }

    public Payout createPayoutForProvider(Long providerId, double amount, String reference) {
        if (amount <= 0.0) {
            throw new BadRequestException("Amount must be positive");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("account_number", "acc_test"); // configured account in RazorpayX dashboard
        Map<String, String> contact = new HashMap<>();
        contact.put("name", "Provider " + providerId);
        payload.put("contact", contact);
        payload.put("amount", (long) Math.round(amount * 100));
        payload.put("currency", props.getRazorpay().getCurrency());
        payload.put("purpose", "payout");
        payload.put("reference_id", reference);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> rpPayout = razorpayXRestTemplate.postForObject(
                RAZORPAYX_API_BASE + "/payouts", req, Map.class);

        if (rpPayout == null || rpPayout.get("id") == null) {
            throw new BadRequestException("Failed to create payout");
        }

        Payout payout = new Payout();
        payout.setRecipientId(providerId);
        payout.setRecipientType(RecipientType.PROVIDER);
        payout.setOrderIds(reference);
        payout.setAmount(amount);
        payout.setCommissionDeducted(0.0);
        payout.setNetAmount(amount);
        payout.setStatus(PayoutStatus.PENDING);
        payout.setTransactionId(String.valueOf(rpPayout.get("id")));
        payout.setPayoutTime(LocalDateTime.now());
        payout.setIsDeleted(false);
        return payoutRepository.save(payout);
    }

    public void verifyAndProcessRazorpayXWebhook(String payload, String signature) {
        String secret = props.getRazorpayx().getWebhookSecret();
        if (!verifySignature(payload, signature, secret)) {
            throw new BadRequestException("Invalid webhook signature");
        }

        String payoutId = extractJsonValue(payload, "id");
        String status = extractJsonValue(payload, "status");
        if (payoutId == null || status == null) return;

        payoutRepository.findByTransactionId(payoutId).ifPresent(p -> {
            if ("processed".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) {
                p.setStatus(PayoutStatus.COMPLETED);
            } else if ("failed".equalsIgnoreCase(status)) {
                p.setStatus(PayoutStatus.FAILED);
            }
            p.setPayoutTime(LocalDateTime.now());
            payoutRepository.save(p);
        });
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



package com.platemate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.platemate.service.PaymentService;
import com.platemate.service.PayoutService;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PayoutService payoutService;

    @PostMapping("/razorpay")
    public ResponseEntity<Void> handleRazorpay(@RequestHeader("X-Razorpay-Signature") String signature,
                                               @RequestBody String payload) {
        paymentService.verifyAndProcessWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/razorpayx")
    public ResponseEntity<Void> handleRazorpayX(@RequestHeader("X-Razorpay-Signature") String signature,
                                                @RequestBody String payload) {
        payoutService.verifyAndProcessRazorpayXWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}



package com.platemate.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.platemate.dto.PayoutDtos;
import com.platemate.exception.ResourceNotFoundException;
import com.platemate.model.Payout;
import com.platemate.model.TiffinProvider;
import com.platemate.model.User;
import com.platemate.repository.TiffinProviderRepository;
import com.platemate.repository.UserRepository;
import com.platemate.service.PayoutService;

@RestController
@RequestMapping("/api")
public class PayoutController {

    @Autowired
    private PayoutService payoutService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TiffinProviderRepository tiffinProviderRepository;

    @GetMapping("/providers/payouts/statements")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<PayoutDtos.StatementResponse> getProviderStatement() {
        TiffinProvider provider = getCurrentProvider();
        LocalDateTime from = null;
        LocalDateTime to = null;
        PayoutDtos.StatementResponse res = payoutService.computeEarningsForProvider(provider.getId(), from, to);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/admin/payouts/run/{providerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Payout> runPayout(@PathVariable Long providerId, @RequestBody PayoutDtos.StatementRequest req) {
        double amount = req.getFrom() != null || req.getTo() != null
                ? payoutService.computeEarningsForProvider(providerId, req.getFrom(), req.getTo()).getNetPayable()
                : payoutService.computeEarningsForProvider(providerId, null, null).getNetPayable();
        String reference = "prov-" + providerId + "-" + System.currentTimeMillis();
        Payout p = payoutService.createPayoutForProvider(providerId, amount, reference);
        return ResponseEntity.ok(p);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private TiffinProvider getCurrentProvider() {
        User user = getCurrentUser();
        TiffinProvider provider = tiffinProviderRepository.findByUser_Id(user.getId());
        if (provider == null) {
            throw new ResourceNotFoundException("Provider profile not found for user");
        }
        return provider;
    }
}



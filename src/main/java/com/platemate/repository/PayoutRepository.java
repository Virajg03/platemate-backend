package com.platemate.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.platemate.enums.RecipientType;
import com.platemate.model.Payout;

public interface PayoutRepository extends JpaRepository<Payout, Long> {
    List<Payout> findAllByIsDeletedFalse();
    
    List<Payout> findAllByRecipientTypeAndRecipientIdAndIsDeletedFalse(RecipientType type, Long recipientId);
    List<Payout> findAllByPayoutTimeBetween(LocalDateTime from, LocalDateTime to);
    Optional<Payout> findByTransactionId(String transactionId);
}



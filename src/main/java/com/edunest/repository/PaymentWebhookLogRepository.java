package com.edunest.repository;

import com.edunest.entity.PaymentWebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentWebhookLogRepository extends JpaRepository<PaymentWebhookLog, Integer> {

    Optional<PaymentWebhookLog> findByPayRequestId(String payRequestId);
}

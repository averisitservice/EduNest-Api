package com.edunest.repository;

import com.edunest.entity.PaymentWebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentWebhookLogRepository extends JpaRepository<PaymentWebhookLog, Integer> {
}

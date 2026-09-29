package com.edunest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_webhook_log", schema = "pay")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_webhook_log_id")
    private Integer paymentWebhookLogId;

    @Column(name = "pay_request_id", nullable = false, unique = true, length = 100)
    private String payRequestId;

    @Column(name = "payment_json", nullable = false, columnDefinition = "TEXT")
    private String paymentJson;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
}

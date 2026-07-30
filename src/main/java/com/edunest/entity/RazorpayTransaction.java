package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "razorpay_transaction", schema = "pay")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "razorpay_transaction_id")
    private Integer razorpayTransactionId;

    @Column(name = "razorpay_order_id", nullable = false)
    private Integer razorpayOrderId;

    @Column(name = "razorpay_payment_id", nullable = false, length = 64)
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature", nullable = false, length = 255)
    private String razorpaySignature;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
}

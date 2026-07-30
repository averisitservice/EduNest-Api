package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "razorpay_order", schema = "pay")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "razorpay_order_id")
    private Integer razorpayOrderId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "razorpay_order_ref", nullable = false, length = 64)
    private String razorpayOrderRef;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "receipt", length = 64)
    private String receipt;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}

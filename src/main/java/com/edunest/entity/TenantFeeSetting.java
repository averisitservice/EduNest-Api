package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenant_fee_setting", schema = "pay")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantFeeSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fee_setting_id")
    private Integer feeSettingId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "academic_year_id", nullable = false)
    private Integer academicYearId;

    // MONTHLY, QUARTERLY, HALF_YEARLY, ANNUAL
    @Column(name = "payment_frequency", nullable = false, length = 50)
    private String paymentFrequency;

    @Column(name = "due_day_of_month", nullable = false)
    @Builder.Default
    private Integer dueDayOfMonth = 10;

    @Column(name = "grace_period_days", nullable = false)
    @Builder.Default
    private Integer gracePeriodDays = 10;

    @Column(name = "overdue_charge_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal overdueChargeAmount = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}

package com.edunest.dto.fee;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TenantFeeSettingRequest {
    private String paymentFrequency; // MONTHLY, QUARTERLY, HALF_YEARLY, ANNUAL
    private Integer dueDayOfMonth;
    private Integer gracePeriodDays;
    private BigDecimal overdueChargeAmount;
}

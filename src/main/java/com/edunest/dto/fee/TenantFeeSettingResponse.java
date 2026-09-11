package com.edunest.dto.fee;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantFeeSettingResponse {
    private Integer feeSettingId;
    private Integer tenantId;
    private Integer academicYearId;
    private String paymentFrequency;
    private Integer dueDayOfMonth;
    private Integer gracePeriodDays;
    private BigDecimal overdueChargeAmount;
}

package com.edunest.dto.fee;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeeStatusResponse {
    private Integer studentId;
    private String studentName;
    private String rollNo;
    private BigDecimal annualFee;
    private BigDecimal overdueCharge;
    private BigDecimal paidAmount;
    private BigDecimal dueAmount;
}

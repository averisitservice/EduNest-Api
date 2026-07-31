package com.edunest.dto.fee;

import lombok.*;

import java.math.BigDecimal;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentFeeDetailResponse {
    private Integer studentId;
    private String studentName;
    private String displayClass;
    private String rollNo;
    private String academicYearName;
    private BigDecimal totalFee;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private List<FeePaymentResponse> payments;
}

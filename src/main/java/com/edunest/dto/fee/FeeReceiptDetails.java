package com.edunest.dto.fee;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeReceiptDetails {

    private String schoolName;
    private String schoolAddress;

    private String receiptNo;
    private String paymentDateFormatted;
    private String admissionNo;
    private String sessionYear;
    private String studentName;
    private String displayClass;
    private String paymentMode;
    private String remarks;
    private String collectedBy;
    private String principalSignUrl;

    private BigDecimal amount;
    private BigDecimal overdueCharge;
}

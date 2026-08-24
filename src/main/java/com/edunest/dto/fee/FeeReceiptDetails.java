package com.edunest.dto.fee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeReceiptDetails {

    private String schoolName;
    private String schoolAddress;
    private String schoolContact;

    private String receiptNo;
    private String paymentDateFormatted;
    private String admissionNo;
    private String sessionYear;
    private String studentName;
    private String displayClass;
    private String paymentMode;
    private String remarks;
    private String collectedBy;

    private BigDecimal amount;
}

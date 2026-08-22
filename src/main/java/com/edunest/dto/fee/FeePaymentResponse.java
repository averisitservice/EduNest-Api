package com.edunest.dto.fee;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeePaymentResponse {
    private Integer feePaymentId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMode;
    private String receiptNo;
    private String remarks;
    private String collectedBy;
    private String receiptUrl;
}

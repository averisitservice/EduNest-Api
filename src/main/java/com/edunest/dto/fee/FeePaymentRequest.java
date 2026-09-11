package com.edunest.dto.fee;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeePaymentRequest {
    private Integer studentId;
    private BigDecimal amount;
    private BigDecimal overdueCharge;
    private LocalDate paymentDate;
    private String paymentMode;
    private String remarks;
}

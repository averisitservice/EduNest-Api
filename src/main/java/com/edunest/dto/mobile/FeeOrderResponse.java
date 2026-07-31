package com.edunest.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeeOrderResponse {
    private Integer razorpayOrderId;
    private String razorpayOrderRef;
    private BigDecimal amount;
    private String currency;
    private String keyId;
}

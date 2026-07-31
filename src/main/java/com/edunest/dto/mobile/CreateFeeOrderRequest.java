package com.edunest.dto.mobile;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateFeeOrderRequest {
    private BigDecimal amount;
}

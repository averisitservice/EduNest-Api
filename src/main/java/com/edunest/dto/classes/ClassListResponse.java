package com.edunest.dto.classes;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassListResponse {
    private Integer classId;
    private String className;
    private Boolean isActive;
    private BigDecimal annualFee;
    private BigDecimal hostelFee;
    private List<String> sections;
    private List<String> subjects;
}
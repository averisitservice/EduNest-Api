package com.edunest.dto.classes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassDTO {
    private Integer classId;
    private String className;
    private BigDecimal annualFee;
    private BigDecimal hostelFee;
    private List<String> sections;
    private List<Integer> subjectIds;
}

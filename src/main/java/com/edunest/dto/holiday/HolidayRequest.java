package com.edunest.dto.holiday;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HolidayRequest {
    private Integer holidayId;
    private String holidayName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String holidayType;
    private String description;
}

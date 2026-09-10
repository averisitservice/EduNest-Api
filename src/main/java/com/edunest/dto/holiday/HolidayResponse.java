package com.edunest.dto.holiday;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayResponse {
    private Integer holidayId;
    private Integer tenantId;
    private Integer academicYearId;
    private String holidayName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String holidayType;
    private String description;
    private Boolean isActive;
}

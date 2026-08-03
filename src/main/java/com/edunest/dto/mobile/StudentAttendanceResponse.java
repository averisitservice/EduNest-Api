package com.edunest.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttendanceResponse {
    private LocalDate fromDate;
    private LocalDate toDate;
    private long presentDays;
    private long absentDays;
    private long lateDays;
    private long totalDays;
    private double percent;
    private List<StudentAttendanceItem> records;
}

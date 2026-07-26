package com.edunest.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentHomeResponse {

    private Integer studentId;
    private String studentName;
    private String photoUrl;
    private String displayClass;
    private String rollNo;
    private String academicYearName;

    private String todayStatus;

    private long presentDays;
    private long absentDays;
    private long lateDays;
    private double thisMonthPercent;
    private double averagePercent;
}

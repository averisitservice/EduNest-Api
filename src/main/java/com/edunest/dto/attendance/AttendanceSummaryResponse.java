package com.edunest.dto.attendance;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryResponse {
    private Integer studentId;
    private String studentName;
    private String rollNo;
    private long presentCount;
    private long absentCount;
    private long lateCount;
    private long halfDayCount;
    private long holidayCount;
    private long totalMarked;
    private double presentPercentage;
}

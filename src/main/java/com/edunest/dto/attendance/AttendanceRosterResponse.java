package com.edunest.dto.attendance;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRosterResponse {
    private LocalDate attendanceDate;
    private List<StudentRow> records;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentRow {
        private Integer studentId;
        private String studentName;
        private String rollNo;
        private String status;
        private String remarks;
        private boolean onLeave;
    }
}

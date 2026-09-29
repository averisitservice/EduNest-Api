package com.edunest.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private long totalStudents;
    private long totalTeachers;
    private long totalClasses;

    private AttendanceToday attendanceToday;
    private BigDecimal feeCollectedThisMonth;

    private List<LatestAnnouncement> latestAnnouncements;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceToday {
        private LocalDate date;
        private long present;
        private long absent;
        private long late;
        private long marked;
        private double presentPercent;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LatestAnnouncement {
        private Integer announcementId;
        private String title;
        private String audience;
        private LocalDate publishDate;
    }
}

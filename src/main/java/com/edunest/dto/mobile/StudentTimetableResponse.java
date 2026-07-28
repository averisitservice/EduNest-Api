package com.edunest.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentTimetableResponse {

    private String displayClass;
    private List<DaySchedule> days;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DaySchedule {
        private String dayName;
        private List<Period> periods;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Period {
        private String slotName;
        private LocalTime startTime;
        private LocalTime endTime;
        private Boolean isBreak;
        private Integer subjectId;
        private String subjectName;
        private String teacherName;
    }
}

package com.edunest.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentExamsResponse {

    private List<ExamItem> upcoming;
    private List<ExamItem> past;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamItem {
        private String examName;
        private Integer subjectId;
        private String subjectName;
        private LocalDate examDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer maxMarks;
        private Integer passMarks;
        private String status;
    }
}

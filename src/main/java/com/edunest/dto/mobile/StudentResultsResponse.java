package com.edunest.dto.mobile;

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
public class StudentResultsResponse {

    private double overallPercentage;
    private BigDecimal overallObtained;
    private Integer overallMax;
    private List<SubjectResult> subjects;
    private List<ExamResultItem> exams;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectResult {
        private Integer subjectId;
        private String subjectName;
        private BigDecimal obtained;
        private Integer max;
        private double percentage;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamResultItem {
        private Integer examId;
        private String examName;
        private LocalDate examDate;
        private BigDecimal obtained;
        private Integer max;
        private double percentage;
        private String grade;
        private String result;
    }
}

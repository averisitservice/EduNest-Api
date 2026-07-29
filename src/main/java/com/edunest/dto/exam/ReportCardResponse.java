package com.edunest.dto.exam;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportCardResponse {
    private Integer studentId;
    private String studentName;
    private String rollNo;
    private String examName;
    private Integer maxMarksPerSubject;
    private Integer passMarks;

    private List<SubjectMark> subjects;

    private BigDecimal totalObtained;
    private Integer totalMax;
    private double percentage;
    private String overallGrade;
    private String result;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectMark {
        private Integer subjectId;
        private String subjectName;
        private BigDecimal marksObtained;
        private String grade;
        private boolean passed;
    }
}

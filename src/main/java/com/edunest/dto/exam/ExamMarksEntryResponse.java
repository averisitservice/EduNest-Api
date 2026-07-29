package com.edunest.dto.exam;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamMarksEntryResponse {
    private Integer examId;
    private String examName;
    private Integer maxMarks;
    private List<SubjectItem> subjects;
    private List<StudentRow> students;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectItem {
        private Integer subjectId;
        private String subjectName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentRow {
        private Integer studentId;
        private String studentName;
        private String rollNo;
        private Map<Integer, BigDecimal> marks;
    }
}

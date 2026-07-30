package com.edunest.dto.exam;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamSummaryResponse {
    private Integer examId;
    private Integer classId;
    private String className;
    private String examName;
    private LocalDate examDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime updatedDate;
}

package com.edunest.dto.exam;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamScheduleResponse {
    private Integer subjectId;
    private String subjectName;
    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxMarks;
    private Integer passMarks;
}

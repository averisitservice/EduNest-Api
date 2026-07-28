package com.edunest.dto.exam;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamScheduleRequest {
    private Integer subjectId;
    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxMarks;
    private Integer passMarks;
}

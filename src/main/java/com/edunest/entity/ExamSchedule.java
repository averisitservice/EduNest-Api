package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "exam_schedule", schema = "school")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_schedule_id")
    private Integer examScheduleId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "exam_id", nullable = false)
    private Integer examId;

    @Column(name = "subject_id", nullable = false)
    private Integer subjectId;

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "max_marks")
    private Integer maxMarks;

    @Column(name = "pass_marks")
    private Integer passMarks;
}

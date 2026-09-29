package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "exam_mark", schema = "school")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamMark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_mark_id")
    private Integer examMarkId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "exam_id", nullable = false)
    private Integer examId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "subject_id", nullable = false)
    private Integer subjectId;

    @Column(name = "academic_year_id", nullable = false)
    private Integer academicYearId;

    @Column(name = "marks_obtained", precision = 6, scale = 2)
    private BigDecimal marksObtained;
}

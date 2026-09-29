package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam", schema = "school")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_id")
    private Integer examId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "class_id", nullable = false)
    private Integer classId;

    @Column(name = "academic_year_id", nullable = false)
    private Integer academicYearId;

    @Column(name = "exam_name", nullable = false, length = 100)
    private String examName;

    @Column(name = "max_marks", nullable = false)
    private Integer maxMarks;

    @Column(name = "pass_marks", nullable = false)
    private Integer passMarks;

    @Column(name = "exam_date")
    private LocalDate examDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "created_by", updatable = false)
    private Integer createdBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "updated_by")
    private Integer updatedBy;
}

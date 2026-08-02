package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_request", schema = "school")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_id")
    private Integer leaveId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "academic_year_id", nullable = false)
    private Integer academicYearId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "class_id")
    private Integer classId;

    @Column(name = "section_id")
    private Integer sectionId;

    @Column(name = "leave_date", nullable = false)
    private LocalDate leaveDate;

    @Column(name = "reason", nullable = false, length = 300)
    private String reason;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "updated_by")
    private Integer updatedBy;
}

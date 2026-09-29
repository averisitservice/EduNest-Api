package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance", schema = "school")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Integer attendanceId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "class_id", nullable = false)
    private Integer classId;

    @Column(name = "section_id")
    private Integer sectionId;

    @Column(name = "academic_year_id", nullable = false)
    private Integer academicYearId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "marked_by")
    private Integer markedBy;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}

package com.edunest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_class", schema = "school")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_class_id")
    private Integer studentClassId;

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

    @Column(name = "roll_no", length = 20)
    private String rollNo;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
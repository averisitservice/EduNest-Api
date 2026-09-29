package com.edunest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "teacher_class", schema = "school")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_class_id")
    private Integer teacherClassId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "teacher_id", nullable = false)
    private Integer teacherId;

    @Column(name = "class_id", nullable = false)
    private Integer classId;

    @Column(name = "section_id")
    private Integer sectionId;

    @Column(name = "academic_year_id")
    private Integer academicYearId;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
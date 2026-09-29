package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "class_subject", schema = "school")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_subject_id")
    private Integer classSubjectId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "class_id", nullable = false)
    private Integer classId;

    @Column(name = "subject_id", nullable = false)
    private Integer subjectId;

    @Column(name = "academic_year_id", nullable = false)
    private Integer academicYearId;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
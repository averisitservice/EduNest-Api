package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teacher_subject", schema = "school")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_subject_id")
    private Integer teacherSubjectId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "teacher_id", nullable = false)
    private Integer teacherId;

    @Column(name = "subject_id", nullable = false)
    private Integer subjectId;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subject", schema = "lookup")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id")
    private Integer subjectId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "subject_name", nullable = false, length = 100)
    private String subjectName;

    @Column(name = "subject_code", nullable = false, length = 20)
    private String subjectCode;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
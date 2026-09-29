package com.edunest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "class_section", schema = "lookup")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Integer sectionId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "class_id", nullable = false)
    private Integer classId;

    @Column(name = "section_name", nullable = false, length = 10)
    private String sectionName;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
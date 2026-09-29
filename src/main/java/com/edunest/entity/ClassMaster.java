package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "class_master", schema = "lookup")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_id")
    private Integer classId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "class_name", nullable = false, length = 50)
    private String className;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
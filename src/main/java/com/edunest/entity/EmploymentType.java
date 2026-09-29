package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employment_type", schema = "lookup")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmploymentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employment_type_id")
    private Integer employmentTypeId;

    @Column(name = "employment_type", nullable = false, unique = true, length = 30)
    private String employmentType;
}
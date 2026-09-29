package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "class_fee", schema = "pay")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_fee_id")
    private Integer classFeeId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "class_id", nullable = false)
    private Integer classId;

    @Column(name = "academic_year_id", nullable = false)
    private Integer academicYearId;

    @Column(name = "annual_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal annualFee;

    @Column(name = "hostel_fee", precision = 10, scale = 2)
    private BigDecimal hostelFee;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
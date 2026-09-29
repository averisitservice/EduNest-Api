package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "teacher", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_id")
    private Integer teacherId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Column(name = "employment_type_id")
    private Integer employmentTypeId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "hashkey", length = 255)
    private String hashkey;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "gender", nullable = false, length = 1)
    private Character gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "mobile_no", nullable = false, length = 15)
    private String mobileNo;

    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Column(name = "qualification", length = 200)
    private String qualification;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "address_line1", length = 150)
    private String addressLine1;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "created_by", nullable = false, updatable = false)
    private Integer createdBy;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    private String teacherName;
}
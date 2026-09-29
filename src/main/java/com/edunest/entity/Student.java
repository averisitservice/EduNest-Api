package com.edunest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "admission_no", nullable = false, unique = true, length = 20)
    private String admissionNo;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "user_name", unique = true, length = 50)
    private String username;

    @Column(name = "gender", nullable = false, length = 1)
    private Character gender;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "aadhar_no", length = 12)
    private String aadharNo;

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "mobile_no", length = 15)
    private String mobileNo;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "hashkey", length = 255)
    private String hashkey;

    @Column(name = "address_line1", length = 150)
    private String addressLine1;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "father_name", length = 100)
    private String fatherName;

    @Column(name = "mother_name", length = 100)
    private String motherName;

    @Column(name = "parent_mobile", length = 15)
    private String parentMobile;

    @Column(name = "parent_email", length = 150)
    private String parentEmail;

    @Column(name = "parent_aadhar", length = 12)
    private String parentAadhar;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_hostel")
    private Boolean isHostel = false;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "created_by", nullable = false, updatable = false)
    private Integer createdBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "updated_by")
    private Integer updatedBy;
}
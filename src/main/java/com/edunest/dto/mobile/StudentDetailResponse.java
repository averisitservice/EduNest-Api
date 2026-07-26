package com.edunest.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentDetailResponse {

    private Integer studentId;
    private String admissionNo;
    private String username;
    private String studentName;
    private String photoUrl;

    private LocalDate dateOfBirth;
    private String gender;
    private String aadharNo;
    private String email;
    private String mobileNo;
    private Boolean isHostel;

    private Integer classId;
    private String className;
    private Integer sectionId;
    private String sectionName;
    private String displayClass;
    private String rollNo;
    private String classTeacherName;

    private String fatherName;
    private String motherName;
    private String parentMobile;
    private String parentEmail;
    private String parentAadhar;

    private String address;
}

package com.edunest.dto.student;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {
    private Integer studentId;
    private String firstName;
    private String middleName;
    private String lastName;
    private Character gender;
    private LocalDate dateOfBirth;
    private String bloodGroup;
    private String aadharNo;
    private String email;
    private String mobileNo;
    private String password;
    private String addressLine1;
    private String city;
    private String state;
    private String postalCode;
    private String fatherName;
    private String motherName;
    private String parentMobile;
    private String parentEmail;
    private String parentAadhar;
    private Integer classId;
    private Integer sectionId;
    private String rollNo;
    private Boolean isHostel;
}

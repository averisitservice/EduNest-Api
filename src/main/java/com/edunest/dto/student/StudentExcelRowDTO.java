package com.edunest.dto.student;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentExcelRowDTO {
    private Integer rowNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private Character gender;
    private LocalDate dateOfBirth;
    private String aadharNo;
    private String email;
    private String mobileNo;
    private String addressLine1;
    private String city;
    private String state;
    private String postalCode;
    private String fatherName;
    private String motherName;
    private String parentMobile;
    private String parentEmail;
    private String parentAadhar;
    private String className;
    private String sectionName;
    private String rollNo;
    private Boolean isHostel;
    private String password;
}

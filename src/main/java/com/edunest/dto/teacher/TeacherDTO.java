package com.edunest.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDTO {
    private Integer teacherId;
    private Integer roleId;
    private String password;
    private String firstName;
    private String lastName;
    private Character gender;
    private LocalDate dateOfBirth;
    private String mobileNo;
    private String email;
    private String qualification;
    private LocalDate joiningDate;
    private String addressLine1;
    private String city;
    private String state;
    private String postalCode;
    private Integer employmentTypeId;
    private List<TeacherClassDTO> teacherClasses;
    private List<TeacherSubjectDTO> teacherSubjects;
}


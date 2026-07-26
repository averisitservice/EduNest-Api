package com.edunest.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileResponse {
    private Integer studentId;
    private String admissionNo;
    private String username;
    private String studentName;
    private String email;
    private String mobileNo;
    private String photoUrl;
    private Boolean isHostel;

    private Integer classId;
    private String className;
    private Integer sectionId;
    private String sectionName;
    private String displayClass;
    private String rollNo;
}

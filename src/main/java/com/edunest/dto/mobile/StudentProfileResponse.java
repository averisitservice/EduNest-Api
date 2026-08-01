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
    private String studentName;
    private String email;
    private String mobileNo;
    private String photoUrl;

    private String displayClass;
    private String rollNo;
}

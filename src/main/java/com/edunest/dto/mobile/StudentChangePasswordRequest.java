package com.edunest.dto.mobile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentChangePasswordRequest {
    private String currentPassword;
    private String newPassword;
}

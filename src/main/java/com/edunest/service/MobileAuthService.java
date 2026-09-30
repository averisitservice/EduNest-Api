package com.edunest.service;

import com.edunest.dto.mobile.*;
import org.springframework.stereotype.Service;

@Service
public interface MobileAuthService {
    StudentLoginResponse studentLogin(StudentLoginRequest request);

    void forgotPassword(StudentForgotPasswordRequest request);

    void changePassword(Integer studentId, StudentChangePasswordRequest request);

    SchoolContactResponse getSchoolContact(Integer tenantId);
}

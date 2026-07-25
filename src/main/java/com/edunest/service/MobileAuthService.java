package com.edunest.service;

import com.edunest.dto.mobile.StudentChangePasswordRequest;
import com.edunest.dto.mobile.StudentForgotPasswordRequest;
import com.edunest.dto.mobile.StudentLoginRequest;
import com.edunest.dto.mobile.StudentLoginResponse;
import org.springframework.stereotype.Service;

@Service
public interface MobileAuthService {
    StudentLoginResponse studentLogin(StudentLoginRequest request);

    void forgotPassword(StudentForgotPasswordRequest request);

    void changePassword(Integer studentId, StudentChangePasswordRequest request);
}

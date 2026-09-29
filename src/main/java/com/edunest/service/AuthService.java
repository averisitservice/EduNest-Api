package com.edunest.service;

import com.edunest.dto.auth.*;

public interface AuthService {
    SchoolLookupResponse getTenantBySchoolCode(String schoolCode);

    LoginResponse login(LoginRequest loginRequest);

    RenewSessionResponse renewSession(RenewSessionRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(Integer teacherId, ResetPasswordRequest request);
}

package com.edunest.service;

import com.edunest.dto.auth.*;

public interface AuthService {
    SchoolLookupResponse getTenantBySchoolCode(String schoolCode);

    LoginResponse login(LoginRequest loginRequest);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(Integer teacherId, ResetPasswordRequest request);

    RenewSessionResponse renewSession(RenewSessionRequest request);
}

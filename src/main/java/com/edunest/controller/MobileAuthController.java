package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.mobile.StudentChangePasswordRequest;
import com.edunest.dto.mobile.StudentForgotPasswordRequest;
import com.edunest.dto.mobile.StudentLoginRequest;
import com.edunest.dto.mobile.StudentLoginResponse;
import com.edunest.service.MobileAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class MobileAuthController {

    @Autowired
    MobileAuthService mobileAuthService;

    @Autowired
    JwtHelper jwtHelper;

    @PostMapping("/login")
    public ResponseEntity<ResponseObject<StudentLoginResponse>> studentLogin(
            @RequestBody StudentLoginRequest request) {

        ResponseObject<StudentLoginResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(mobileAuthService.studentLogin(request));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseObject<String>> forgotPassword(
            @RequestBody StudentForgotPasswordRequest request) {

        mobileAuthService.forgotPassword(request);

        ResponseObject<String> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData("A new password has been sent to your registered email address.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ResponseObject<String>> changePassword(
            HttpServletRequest httpRequest, @RequestBody StudentChangePasswordRequest request) {

        String token = jwtHelper.cleanToken(httpRequest.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);

        mobileAuthService.changePassword(studentId, request);

        ResponseObject<String> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData("Your password has been changed successfully.");

        return ResponseEntity.ok(response);
    }
}

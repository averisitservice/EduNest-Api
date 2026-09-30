package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.mobile.*;
import com.edunest.service.MobileAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/school/contact")
    public ResponseEntity<ResponseObject<SchoolContactResponse>> getSchoolContact(HttpServletRequest request) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<SchoolContactResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(mobileAuthService.getSchoolContact(tenantId));

        return ResponseEntity.ok(response);
    }
}

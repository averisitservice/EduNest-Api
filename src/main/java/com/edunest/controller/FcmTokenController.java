package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.notification.FcmTokenRequest;
import com.edunest.service.FcmTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/fcm-token")
public class FcmTokenController {

    @Autowired
    FcmTokenService fcmTokenService;

    @Autowired
    JwtHelper jwtHelper;

    @PostMapping
    public ResponseEntity<ResponseObject<Boolean>> saveToken(
            HttpServletRequest request, @RequestBody FcmTokenRequest fcmTokenRequest) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<Boolean> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(fcmTokenService.saveToken(tenantId, studentId, fcmTokenRequest));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<ResponseObject<Boolean>> deleteToken(
            HttpServletRequest request, @RequestParam String fcmToken) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<Boolean> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(fcmTokenService.deleteToken(tenantId, studentId, fcmToken));
        return ResponseEntity.ok(response);
    }
}

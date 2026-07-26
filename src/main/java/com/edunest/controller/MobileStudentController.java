package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.mobile.StudentDetailResponse;
import com.edunest.dto.mobile.StudentHomeResponse;
import com.edunest.service.MobileStudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class MobileStudentController {

    @Autowired
    MobileStudentService mobileStudentService;

    @Autowired
    JwtHelper jwtHelper;

    @GetMapping("/home")
    public ResponseEntity<ResponseObject<StudentHomeResponse>> getStudentHome(HttpServletRequest request) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<StudentHomeResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(mobileStudentService.getStudentHome(studentId, tenantId));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<ResponseObject<StudentDetailResponse>> getStudentDetailsById(
            HttpServletRequest request, @PathVariable Integer studentId) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<StudentDetailResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(mobileStudentService.getStudentDetailsById(studentId, tenantId));

        return ResponseEntity.ok(response);
    }
}

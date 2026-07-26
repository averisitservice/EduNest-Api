package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.mobile.SchoolContactResponse;
import com.edunest.service.MobileSchoolService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/school")
public class MobileSchoolController {

    @Autowired
    MobileSchoolService mobileSchoolService;

    @Autowired
    JwtHelper jwtHelper;

    @GetMapping("/contact")
    public ResponseEntity<ResponseObject<SchoolContactResponse>> getSchoolContact(HttpServletRequest request) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<SchoolContactResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(mobileSchoolService.getSchoolContact(tenantId));

        return ResponseEntity.ok(response);
    }
}

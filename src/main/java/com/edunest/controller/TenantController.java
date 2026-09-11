package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.entity.TenantFeeSetting;
import com.edunest.service.TenantService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tenant")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private JwtHelper jwtHelper;

    @GetMapping("/fee-setting")
    public ResponseEntity<ResponseObject<TenantFeeSetting>> getFeeSetting(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<TenantFeeSetting> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(tenantService.getFeeSetting(tenantId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fee-setting")
    public ResponseEntity<ResponseObject<Boolean>> saveFeeSetting(
            HttpServletRequest request,
            @RequestBody TenantFeeSetting settingRequest) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<Boolean> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(tenantService.saveFeeSetting(tenantId, settingRequest));
        return ResponseEntity.ok(response);
    }
}

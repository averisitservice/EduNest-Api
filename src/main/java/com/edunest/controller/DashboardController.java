package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.dashboard.DashboardSummaryResponse;
import com.edunest.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    DashboardService dashboardService;

    @Autowired
    JwtHelper jwtHelper;

    @GetMapping("/summary")
    public ResponseEntity<ResponseObject<DashboardSummaryResponse>> getDashboardSummary(HttpServletRequest request) {
        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<DashboardSummaryResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(dashboardService.getDashboardSummary(tenantId));
        return ResponseEntity.ok(response);
    }
}

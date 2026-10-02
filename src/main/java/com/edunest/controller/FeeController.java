package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.fee.FeePaymentRequest;
import com.edunest.dto.fee.FeePaymentResponse;
import com.edunest.dto.fee.FeeStatusResponse;
import com.edunest.service.FeeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fee")
public class FeeController {

    @Autowired
    FeeService feeService;

    @Autowired
    JwtHelper jwtHelper;

    @GetMapping("/status/{classId}")
    public ResponseEntity<ResponseObject<List<FeeStatusResponse>>> getFeeStatus(
            HttpServletRequest request, @PathVariable Integer classId, @RequestParam(required = false) Integer sectionId) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<List<FeeStatusResponse>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(feeService.getFeeStatus(tenantId, classId, sectionId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payment")
    public ResponseEntity<ResponseObject<String>> collectPayment(HttpServletRequest request, @RequestBody FeePaymentRequest paymentRequest) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);
        Integer collectedBy = jwtHelper.extractTeacherId(token);

        ResponseObject<String> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(feeService.collectPayment(tenantId, collectedBy, paymentRequest));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{studentId}")
    public ResponseEntity<ResponseObject<List<FeePaymentResponse>>> getPaymentHistory(HttpServletRequest request, @PathVariable Integer studentId) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<List<FeePaymentResponse>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(feeService.getPaymentHistory(tenantId, studentId));
        return ResponseEntity.ok(response);
    }
}

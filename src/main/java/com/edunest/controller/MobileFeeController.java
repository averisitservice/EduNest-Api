package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.fee.StudentFeeDetailResponse;
import com.edunest.dto.mobile.CreateFeeOrderRequest;
import com.edunest.dto.mobile.FeeOrderResponse;
import com.edunest.dto.mobile.VerifyPaymentRequest;
import com.edunest.dto.mobile.VerifyPaymentResponse;
import com.edunest.service.FeeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/student/fee")
public class MobileFeeController {

    @Autowired
    FeeService feeService;

    @Autowired
    JwtHelper jwtHelper;

    @GetMapping("/detail")
    public ResponseEntity<ResponseObject<StudentFeeDetailResponse>> getFeeDetail(HttpServletRequest request) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<StudentFeeDetailResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(feeService.getStudentFeeDetail(tenantId, studentId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-order")
    public ResponseEntity<ResponseObject<FeeOrderResponse>> createOrder(
            HttpServletRequest request,
            @RequestBody(required = false) CreateFeeOrderRequest createFeeOrderRequest) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        BigDecimal requestedAmount = createFeeOrderRequest != null ? createFeeOrderRequest.getAmount() : null;

        ResponseObject<FeeOrderResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(feeService.createFeeOrder(tenantId, studentId, requestedAmount));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<ResponseObject<VerifyPaymentResponse>> verifyPayment(
            @RequestBody VerifyPaymentRequest verifyPaymentRequest) {

        VerifyPaymentResponse verifyPaymentResponse = feeService.verifyFeePayment(
                verifyPaymentRequest.getRazorpayOrderId(),
                verifyPaymentRequest.getRazorpayPaymentId(),
                verifyPaymentRequest.getRazorpaySignature());

        ResponseObject<VerifyPaymentResponse> response = new ResponseObject<>();
        response.setSuccess(verifyPaymentResponse.isVerified());
        response.setData(verifyPaymentResponse);
        return ResponseEntity.ok(response);
    }
}

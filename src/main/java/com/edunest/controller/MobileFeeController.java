package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.mobile.FeeOrderResponse;
import com.edunest.dto.mobile.VerifyPaymentRequest;
import com.edunest.dto.mobile.VerifyPaymentResponse;
import com.edunest.entity.RazorpayOrder;
import com.edunest.service.RazorpayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/student/fee")
public class MobileFeeController {

    private static final BigDecimal STATIC_FEE_AMOUNT = BigDecimal.valueOf(1000);

    @Autowired
    RazorpayService razorpayService;

    @Autowired
    JwtHelper jwtHelper;

    @PostMapping("/create-order")
    public ResponseEntity<ResponseObject<FeeOrderResponse>> createOrder(HttpServletRequest request) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        String receipt = "FEE-" + studentId + "-" + System.currentTimeMillis();
        RazorpayOrder razorpayOrder = razorpayService.createOrder(tenantId, studentId, STATIC_FEE_AMOUNT, "INR", receipt);

        FeeOrderResponse feeOrderResponse = new FeeOrderResponse();
        feeOrderResponse.setRazorpayOrderId(razorpayOrder.getRazorpayOrderId());
        feeOrderResponse.setRazorpayOrderRef(razorpayOrder.getRazorpayOrderRef());
        feeOrderResponse.setAmount(razorpayOrder.getAmount());
        feeOrderResponse.setCurrency(razorpayOrder.getCurrency());
        feeOrderResponse.setKeyId(razorpayService.getKeyId());

        ResponseObject<FeeOrderResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(feeOrderResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<ResponseObject<VerifyPaymentResponse>> verifyPayment(
            @RequestBody VerifyPaymentRequest verifyPaymentRequest) {

        boolean verified = razorpayService.verifyAndRecordPayment(
                verifyPaymentRequest.getRazorpayOrderId(),
                verifyPaymentRequest.getRazorpayPaymentId(),
                verifyPaymentRequest.getRazorpaySignature());

        VerifyPaymentResponse verifyPaymentResponse = new VerifyPaymentResponse();
        verifyPaymentResponse.setVerified(verified);
        verifyPaymentResponse.setStatus(verified ? "PAID" : "FAILED");

        ResponseObject<VerifyPaymentResponse> response = new ResponseObject<>();
        response.setSuccess(verified);
        response.setData(verifyPaymentResponse);
        return ResponseEntity.ok(response);
    }
}

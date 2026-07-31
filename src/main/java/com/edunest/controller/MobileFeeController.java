package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.fee.StudentFeeDetailResponse;
import com.edunest.dto.mobile.CreateFeeOrderRequest;
import com.edunest.dto.mobile.FeeOrderResponse;
import com.edunest.dto.mobile.VerifyPaymentRequest;
import com.edunest.dto.mobile.VerifyPaymentResponse;
import com.edunest.entity.RazorpayOrder;
import com.edunest.error.CustomException;
import com.edunest.service.FeeService;
import com.edunest.service.RazorpayService;
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
    RazorpayService razorpayService;

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

        BigDecimal pendingAmount = feeService.getStudentFeeDetail(tenantId, studentId).getPendingAmount();
        if (pendingAmount == null || pendingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("amount", "No pending fee to pay");
        }

        BigDecimal amount = createFeeOrderRequest != null ? createFeeOrderRequest.getAmount() : null;
        if (amount == null) {
            amount = pendingAmount;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("amount", "Amount must be greater than zero");
        }
        if (amount.compareTo(pendingAmount) > 0) {
            throw new CustomException("amount", "Amount cannot be more than the pending fee");
        }

        String receipt = "FEE-" + studentId + "-" + System.currentTimeMillis();
        RazorpayOrder razorpayOrder = razorpayService.createOrder(tenantId, studentId, amount, "INR", receipt);

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

        if (verified) {
            RazorpayOrder order = razorpayService.getOrder(verifyPaymentRequest.getRazorpayOrderId());
            feeService.recordOnlinePayment(order.getTenantId(), order.getStudentId(), order.getAmount(),
                    verifyPaymentRequest.getRazorpayPaymentId());
        }

        VerifyPaymentResponse verifyPaymentResponse = new VerifyPaymentResponse();
        verifyPaymentResponse.setVerified(verified);
        verifyPaymentResponse.setStatus(verified ? "PAID" : "FAILED");

        ResponseObject<VerifyPaymentResponse> response = new ResponseObject<>();
        response.setSuccess(verified);
        response.setData(verifyPaymentResponse);
        return ResponseEntity.ok(response);
    }
}

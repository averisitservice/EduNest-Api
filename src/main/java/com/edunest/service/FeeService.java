package com.edunest.service;

import com.edunest.dto.fee.FeePaymentRequest;
import com.edunest.dto.fee.FeePaymentResponse;
import com.edunest.dto.fee.FeeStatusResponse;
import com.edunest.dto.fee.StudentFeeDetailResponse;

import java.math.BigDecimal;
import java.util.List;

public interface FeeService {

    List<FeeStatusResponse> getFeeStatus(Integer tenantId, Integer classId, Integer sectionId);

    String collectPayment(Integer tenantId, Integer collectedBy, FeePaymentRequest request);

    List<FeePaymentResponse> getPaymentHistory(Integer tenantId, Integer studentId);

    StudentFeeDetailResponse getStudentFeeDetail(Integer tenantId, Integer studentId);

    void recordOnlinePayment(Integer tenantId, Integer studentId, BigDecimal amount, String razorpayPaymentId);
}

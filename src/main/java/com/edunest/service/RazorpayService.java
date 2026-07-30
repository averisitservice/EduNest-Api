package com.edunest.service;

import com.edunest.entity.RazorpayOrder;
import com.edunest.entity.RazorpayTransaction;

import java.math.BigDecimal;

public interface RazorpayService {

    RazorpayOrder createOrder(Integer tenantId, Integer studentId, BigDecimal amount, String currency, String receipt);

    boolean verifySignature(String razorpayOrderRef, String razorpayPaymentId, String razorpaySignature);

    RazorpayTransaction recordTransaction(Integer razorpayOrderId, String razorpayPaymentId,
                                           String razorpaySignature, String status, String failureReason);

    void saveWebhookPayload(String payRequestId, String paymentJson);
}

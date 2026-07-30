package com.edunest.service;

import com.edunest.entity.PaymentWebhookLog;
import com.edunest.entity.RazorpayOrder;
import com.edunest.entity.RazorpayTransaction;
import com.edunest.error.CustomException;
import com.edunest.repository.PaymentWebhookLogRepository;
import com.edunest.repository.RazorpayOrderRepository;
import com.edunest.repository.RazorpayTransactionRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class RazorpayServiceImpl implements RazorpayService {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    private RazorpayClient razorpayClient;

    @Autowired
    RazorpayOrderRepository razorpayOrderRepository;

    @Autowired
    RazorpayTransactionRepository razorpayTransactionRepository;

    @Autowired
    PaymentWebhookLogRepository paymentWebhookLogRepository;

    @PostConstruct
    private void init() throws RazorpayException {
        this.razorpayClient = new RazorpayClient(keyId, keySecret);
    }

    @Override
    public RazorpayOrder createOrder(Integer tenantId, Integer studentId, BigDecimal amount, String currency, String receipt) {
        String resolvedCurrency = StringUtils.hasText(currency) ? currency : "INR";
        long amountInPaise = amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", resolvedCurrency);
            orderRequest.put("receipt", receipt);

            Order order = razorpayClient.orders.create(orderRequest);

            RazorpayOrder razorpayOrder = new RazorpayOrder();
            razorpayOrder.setTenantId(tenantId);
            razorpayOrder.setStudentId(studentId);
            razorpayOrder.setRazorpayOrderRef(order.get("id"));
            razorpayOrder.setAmount(amount);
            razorpayOrder.setCurrency(resolvedCurrency);
            razorpayOrder.setReceipt(receipt);
            razorpayOrder.setStatus("created");

            return razorpayOrderRepository.save(razorpayOrder);
        } catch (RazorpayException e) {
            throw new CustomException("razorpayOrder", "Failed to create Razorpay order: " + e.getMessage());
        }
    }

    @Override
    public boolean verifySignature(String razorpayOrderRef, String razorpayPaymentId, String razorpaySignature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal((razorpayOrderRef + "|" + razorpayPaymentId).getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            String expectedSignature = hex.toString();

            byte[] expected = expectedSignature.getBytes(StandardCharsets.UTF_8);
            byte[] actual = (razorpaySignature != null ? razorpaySignature : "").getBytes(StandardCharsets.UTF_8);
            if (expected.length != actual.length) {
                return false;
            }
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) {
            throw new CustomException("razorpaySignature", "Failed to verify Razorpay signature: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public RazorpayTransaction recordTransaction(Integer razorpayOrderId, String razorpayPaymentId,
                                                  String razorpaySignature, String status, String failureReason) {
        RazorpayOrder razorpayOrder = razorpayOrderRepository.findById(razorpayOrderId)
                .orElseThrow(() -> new CustomException("razorpayOrderId", "Razorpay order not found"));

        RazorpayTransaction transaction = new RazorpayTransaction();
        transaction.setRazorpayOrderId(razorpayOrderId);
        transaction.setRazorpayPaymentId(razorpayPaymentId);
        transaction.setRazorpaySignature(razorpaySignature);
        transaction.setStatus(status);
        transaction.setFailureReason(failureReason);
        RazorpayTransaction savedTransaction = razorpayTransactionRepository.save(transaction);

        razorpayOrder.setStatus(status);
        razorpayOrderRepository.save(razorpayOrder);

        return savedTransaction;
    }

    @Override
    public void saveWebhookPayload(String payRequestId, String paymentJson) {
        PaymentWebhookLog log = new PaymentWebhookLog();
        log.setPayRequestId(payRequestId);
        log.setPaymentJson(paymentJson);
        paymentWebhookLogRepository.save(log);
    }
}

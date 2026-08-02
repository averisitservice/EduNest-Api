package com.edunest.configuration;

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
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Configuration
public class RazorpayConfiguration {

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
    private void init() {
        this.razorpayClient = razorpayClient();
    }

    private RazorpayClient razorpayClient() {
        try {
            return new RazorpayClient(keyId, keySecret);
        } catch (RazorpayException e) {
            throw new CustomException("razorpay", "Failed to initialize Razorpay client: " + e.getMessage());
        }
    }

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

            RazorpayOrder saved = razorpayOrderRepository.save(razorpayOrder);
            log.info("Successfully created Razorpay order: razorpayOrderRef={}, amount={}", saved.getRazorpayOrderRef(), amount);
            return saved;
        } catch (RazorpayException e) {
            log.error("Error creating Razorpay order: studentId={}, amount={}, error={}", studentId, amount, e.getMessage(), e);
            throw new CustomException("razorpayOrder", "Failed to create Razorpay order: " + e.getMessage());
        }
    }

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
            log.error("Error verifying Razorpay signature: razorpayOrderRef={}, error={}", razorpayOrderRef, e.getMessage(), e);
            throw new CustomException("razorpaySignature", "Failed to verify Razorpay signature: " + e.getMessage());
        }
    }

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

        log.info("Recorded Razorpay transaction: razorpayOrderId={}, paymentId={}, status={}", razorpayOrderId, razorpayPaymentId, status);
        return savedTransaction;
    }

    public void saveWebhookPayload(String payRequestId, String paymentJson) {
        PaymentWebhookLog webhookLog = new PaymentWebhookLog();
        webhookLog.setPayRequestId(payRequestId);
        webhookLog.setPaymentJson(paymentJson);
        paymentWebhookLogRepository.save(webhookLog);
    }

    @Transactional
    public boolean verifyAndRecordPayment(Integer razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        RazorpayOrder razorpayOrder = razorpayOrderRepository.findById(razorpayOrderId)
                .orElseThrow(() -> new CustomException("razorpayOrderId", "Razorpay order not found"));

        boolean isValid = verifySignature(razorpayOrder.getRazorpayOrderRef(), razorpayPaymentId, razorpaySignature);

        recordTransaction(razorpayOrderId, razorpayPaymentId, razorpaySignature,
                isValid ? "PAID" : "FAILED",
                isValid ? null : "Signature verification failed");

        return isValid;
    }

    public RazorpayOrder getOrder(Integer razorpayOrderId) {
        return razorpayOrderRepository.findById(razorpayOrderId)
                .orElseThrow(() -> new CustomException("razorpayOrderId", "Razorpay order not found"));
    }

    public String getKeyId() {
        return keyId;
    }
}

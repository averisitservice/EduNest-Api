package com.edunest.configuration;

import com.edunest.repository.StudentDeviceTokenRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    private static final int MAX_TOKENS_PER_BATCH = 500;

    @Value("${firebase.credentials-file:}")
    private String credentialsFile;

    @Autowired
    StudentDeviceTokenRepository studentDeviceTokenRepository;

    private boolean enabled = false;

    @PostConstruct
    public void initialize() {
        if (credentialsFile == null || credentialsFile.isBlank()) {
            log.warn("firebase.credentials-file is not set; push notifications are disabled.");
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            enabled = true;
            return;
        }

        try (InputStream serviceAccount = new ClassPathResource(credentialsFile).getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            enabled = true;
            log.info("Firebase Admin SDK initialized; push notifications are enabled.");
        } catch (Exception e) {
            log.error("Failed to initialize Firebase from '{}'; push notifications are disabled: {}",
                    credentialsFile, e.getMessage());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void sendToStudents(Integer tenantId, List<Integer> studentIds, String title, String body,
                                Map<String, String> data) {
        if (!enabled) {
            log.debug("Push notification skipped (Firebase disabled): studentIds={}, title={}", studentIds, title);
            return;
        }
        if (studentIds == null || studentIds.isEmpty()) {
            return;
        }

        List<String> tokens =
                studentDeviceTokenRepository.findDistinctFcmTokenByTenantIdAndStudentIdIn(tenantId, studentIds);
        if (tokens.isEmpty()) {
            return;
        }

        Map<String, String> payload = new HashMap<>();
        if (data != null) {
            payload.putAll(data);
        }
        payload.putIfAbsent("type", "NOTIFICATION");
        payload.put("title", title);
        payload.put("body", body);

        for (int i = 0; i < tokens.size(); i += MAX_TOKENS_PER_BATCH) {
            List<String> batch = tokens.subList(i, Math.min(i + MAX_TOKENS_PER_BATCH, tokens.size()));
            sendBatch(batch, title, body, payload);
        }
    }

    private void sendBatch(List<String> tokens, String title, String body, Map<String, String> data) {
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .putAllData(data)
                .addAllTokens(tokens)
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            if (response.getFailureCount() > 0) {
                removeInvalidTokens(tokens, response.getResponses());
            }
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM push notification batch", e);
        }
    }

    private void removeInvalidTokens(List<String> tokens, List<SendResponse> responses) {
        List<String> deadTokens = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (sendResponse.isSuccessful()) {
                continue;
            }
            MessagingErrorCode errorCode = sendResponse.getException() != null
                    ? sendResponse.getException().getMessagingErrorCode() : null;
            if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                deadTokens.add(tokens.get(i));
            }
        }
        for (String deadToken : deadTokens) {
            studentDeviceTokenRepository.deleteByFcmToken(deadToken);
        }
    }
}

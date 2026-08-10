package com.edunest.service;

import com.edunest.configuration.FirebaseConfig;
import com.edunest.repository.StudentDeviceTokenRepository;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FcmPushServiceImpl implements FcmPushService {

    private static final Logger log = LoggerFactory.getLogger(FcmPushServiceImpl.class);

    // FCM allows a maximum of 500 tokens per multicast call
    private static final int BATCH_SIZE = 500;

    @Autowired
    StudentDeviceTokenRepository studentDeviceTokenRepository;

    @Autowired
    FirebaseConfig firebaseConfig;

    @Override
    public void sendToStudents(Integer tenantId, List<Integer> studentIds, String title, String body,
                                Map<String, String> data) {
        if (!firebaseConfig.isEnabled()) {
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

        for (int i = 0; i < tokens.size(); i += BATCH_SIZE) {
            List<String> batch = tokens.subList(i, Math.min(i + BATCH_SIZE, tokens.size()));
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
        deadTokens.forEach(studentDeviceTokenRepository::deleteByFcmToken);
    }
}

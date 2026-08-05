package com.edunest.service;

import com.edunest.configuration.FirebaseConfiguration;
import com.edunest.entity.DeviceToken;
import com.edunest.repository.DeviceTokenRepository;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PushNotificationServiceImpl implements PushNotificationService {

    @Autowired
    FirebaseConfiguration firebaseConfiguration;

    @Autowired
    DeviceTokenRepository deviceTokenRepository;

    @Override
    public void sendToStudent(Integer tenantId, Integer studentId, String title, String body, Map<String, String> data) {
        if (!firebaseConfiguration.isEnabled()) {
            log.debug("Push notification skipped (Firebase disabled): studentId={}, title={}", studentId, title);
            return;
        }

        List<DeviceToken> deviceTokens = deviceTokenRepository.findByTenantIdAndStudentId(tenantId, studentId);
        if (deviceTokens.isEmpty()) {
            return;
        }

        List<String> tokens = deviceTokens.stream().map(DeviceToken::getFcmToken).toList();

        MulticastMessage.Builder builder = MulticastMessage.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .addAllTokens(tokens);
        if (data != null) {
            builder.putAllData(data);
        }

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(builder.build());
            pruneInvalidTokens(tokens, response);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push notification to studentId={}: {}", studentId, e.getMessage());
        }
    }

    private void pruneInvalidTokens(List<String> tokens, BatchResponse response) {
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (sendResponse.isSuccessful()) {
                continue;
            }

            FirebaseMessagingException exception = sendResponse.getException();
            MessagingErrorCode errorCode = exception != null ? exception.getMessagingErrorCode() : null;
            if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                deviceTokenRepository.deleteByFcmToken(tokens.get(i));
            }
        }
    }
}

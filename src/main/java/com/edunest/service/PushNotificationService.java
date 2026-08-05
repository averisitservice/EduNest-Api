package com.edunest.service;

import java.util.Map;

public interface PushNotificationService {

    void sendToStudent(Integer tenantId, Integer studentId, String title, String body, Map<String, String> data);
}

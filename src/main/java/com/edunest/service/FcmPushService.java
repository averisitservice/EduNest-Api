package com.edunest.service;

import java.util.List;
import java.util.Map;

public interface FcmPushService {

    /**
     * Sends a push notification to every registered device of the given students.
     * The data payload always carries "type" so the mobile app knows which screen to open on tap.
     */
    void sendToStudents(Integer tenantId, List<Integer> studentIds, String title, String body,
                         Map<String, String> data);
}

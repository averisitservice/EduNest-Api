package com.edunest.service;

import com.edunest.dto.notification.FcmTokenRequest;

public interface FcmTokenService {

    boolean saveToken(Integer tenantId, Integer studentId, FcmTokenRequest request);

    boolean deleteToken(Integer tenantId, Integer studentId, String fcmToken);
}

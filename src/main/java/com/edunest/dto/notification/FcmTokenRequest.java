package com.edunest.dto.notification;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FcmTokenRequest {

    private String fcmToken;
    private String deviceId;
    private String platform;
}

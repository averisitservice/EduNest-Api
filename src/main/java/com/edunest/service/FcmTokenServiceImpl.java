package com.edunest.service;

import com.edunest.dto.notification.FcmTokenRequest;
import com.edunest.entity.StudentDeviceToken;
import com.edunest.error.CustomException;
import com.edunest.repository.StudentDeviceTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class FcmTokenServiceImpl implements FcmTokenService {

    @Autowired
    StudentDeviceTokenRepository studentDeviceTokenRepository;

    @Override
    @Transactional
    public boolean saveToken(Integer tenantId, Integer studentId, FcmTokenRequest request) {
        if (!StringUtils.hasText(request.getFcmToken())) {
            throw new CustomException("fcmToken", "FCM token is required");
        }

        // A token is unique per device, so re-registering the same device (even for a
        // different student, e.g. shared device or account switch) just re-points it.
        StudentDeviceToken deviceToken = studentDeviceTokenRepository
                .findByFcmToken(request.getFcmToken())
                .orElseGet(StudentDeviceToken::new);

        deviceToken.setTenantId(tenantId);
        deviceToken.setStudentId(studentId);
        deviceToken.setFcmToken(request.getFcmToken());
        deviceToken.setDeviceId(request.getDeviceId());
        deviceToken.setPlatform(request.getPlatform());
        deviceToken.setUpdatedDate(LocalDateTime.now());

        studentDeviceTokenRepository.save(deviceToken);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteToken(Integer tenantId, Integer studentId, String fcmToken) {
        if (!StringUtils.hasText(fcmToken)) {
            throw new CustomException("fcmToken", "FCM token is required");
        }
        studentDeviceTokenRepository.deleteByTenantIdAndStudentIdAndFcmToken(tenantId, studentId, fcmToken);
        return true;
    }
}

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

        StudentDeviceToken studentDeviceToken = studentDeviceTokenRepository.findByFcmToken(request.getFcmToken()).orElseGet(StudentDeviceToken::new);

        studentDeviceToken.setTenantId(tenantId);
        studentDeviceToken.setStudentId(studentId);
        studentDeviceToken.setFcmToken(request.getFcmToken());
        studentDeviceToken.setDeviceId(request.getDeviceId());
        studentDeviceToken.setPlatform(request.getPlatform());
        studentDeviceToken.setUpdatedDate(LocalDateTime.now());

        studentDeviceTokenRepository.save(studentDeviceToken);
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

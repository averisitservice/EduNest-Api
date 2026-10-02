package com.edunest.service;

import com.edunest.configuration.FirebaseConfig;
import com.edunest.constant.Constant;
import com.edunest.entity.StudentNotification;
import com.edunest.repository.StudentNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentNotificationServiceImpl implements StudentNotificationService {

    @Autowired
    StudentNotificationRepository studentNotificationRepository;

    @Autowired
    FirebaseConfig firebaseConfig;

    @Override
    @Transactional
    public void notify(Integer tenantId, List<Integer> studentIds, String type, Integer referenceId, String title, String body) {
        if (studentIds == null || studentIds.isEmpty()) {
            return;
        }

        Map<Integer, StudentNotification> existingByStudentId = new HashMap<>();
        if (referenceId != null) {
            for (StudentNotification existing : studentNotificationRepository
                    .findByTenantIdAndTypeAndReferenceIdAndStudentIdIn(tenantId, type, referenceId, studentIds)) {
                existingByStudentId.put(existing.getStudentId(), existing);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        List<StudentNotification> notifications = new ArrayList<>();
        for (Integer studentId : studentIds) {
            StudentNotification notification = existingByStudentId.get(studentId);
            if (notification == null) {
                notification = new StudentNotification();
                notification.setTenantId(tenantId);
                notification.setStudentId(studentId);
                notification.setType(type);
                notification.setReferenceId(referenceId);
            }
            notification.setTitle(title);
            notification.setBody(body);
            notification.setIsRead(false);
            notification.setUpdatedDate(now);
            notifications.add(notification);
        }
        studentNotificationRepository.saveAll(notifications);

        Map<String, String> data = new HashMap<>();
        data.put("type", Constant.PUSH_TYPE_NOTIFICATION);
        data.put("category", type);
        if (referenceId != null) {
            data.put("referenceId", String.valueOf(referenceId));
        }

        firebaseConfig.sendToStudents(tenantId, studentIds, title, body, data);
    }
}

package com.edunest.service;

import com.edunest.common.PagedResponse;
import com.edunest.dto.mobile.StudentNotificationItem;
import com.edunest.entity.StudentNotification;
import com.edunest.error.CustomException;
import com.edunest.repository.StudentNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentNotificationServiceImpl implements StudentNotificationService {

    @Autowired
    StudentNotificationRepository studentNotificationRepository;

    @Autowired
    FcmPushService fcmPushService;

    @Override
    @Transactional
    public void notify(Integer tenantId, List<Integer> studentIds, String type, Integer referenceId, String title, String body) {
        if (studentIds == null || studentIds.isEmpty()) {
            return;
        }

        List<StudentNotification> notifications = new ArrayList<>();
        for (Integer studentId : studentIds) {
            StudentNotification notification = new StudentNotification();
            notification.setTenantId(tenantId);
            notification.setStudentId(studentId);
            notification.setType(type);
            notification.setReferenceId(referenceId);
            notification.setTitle(title);
            notification.setBody(body);
            notification.setIsRead(false);
            notifications.add(notification);
        }
        studentNotificationRepository.saveAll(notifications);

        Map<String, String> data = new HashMap<>();
        data.put("type", "NOTIFICATION");
        data.put("category", type);
        if (referenceId != null) {
            data.put("referenceId", String.valueOf(referenceId));
        }

        fcmPushService.sendToStudents(tenantId, studentIds, title, body, data);
    }

    @Override
    public PagedResponse<StudentNotificationItem> getNotifications(Integer tenantId, Integer studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentNotification> notificationPage = studentNotificationRepository
                .findByTenantIdAndStudentIdOrderByCreatedDateDescStudentNotificationIdDesc(tenantId, studentId, pageable);

        List<StudentNotificationItem> items = new ArrayList<>();
        for (StudentNotification notification : notificationPage.getContent()) {
            StudentNotificationItem item = new StudentNotificationItem();
            item.setNotificationId(notification.getStudentNotificationId());
            item.setType(notification.getType());
            item.setReferenceId(notification.getReferenceId());
            item.setTitle(notification.getTitle());
            item.setBody(notification.getBody());
            item.setIsRead(notification.getIsRead());
            item.setCreatedDate(notification.getCreatedDate());
            items.add(item);
        }

        return new PagedResponse<>(items, notificationPage.getTotalElements(), notificationPage.getTotalPages(),
                notificationPage.getNumber(), notificationPage.getSize());
    }

    @Override
    @Transactional
    public boolean markAsRead(Integer tenantId, Integer studentId, Integer notificationId) {
        StudentNotification notification = studentNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException("notificationId", "Notification not found"));

        if (!notification.getTenantId().equals(tenantId) || !notification.getStudentId().equals(studentId)) {
            throw new CustomException("notificationId", "Notification not found");
        }

        notification.setIsRead(true);
        studentNotificationRepository.save(notification);
        return true;
    }
}

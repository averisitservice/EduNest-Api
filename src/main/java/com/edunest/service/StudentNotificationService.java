package com.edunest.service;

import com.edunest.common.PagedResponse;
import com.edunest.dto.mobile.StudentNotificationItem;

import java.util.List;

public interface StudentNotificationService {

    void notify(Integer tenantId, List<Integer> studentIds, String type, Integer referenceId, String title, String body);

    PagedResponse<StudentNotificationItem> getNotifications(Integer tenantId, Integer studentId, int page, int size);

    boolean markAsRead(Integer tenantId, Integer studentId, Integer notificationId);
}

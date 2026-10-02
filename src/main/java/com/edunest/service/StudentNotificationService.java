package com.edunest.service;

import java.util.List;

public interface StudentNotificationService {

    void notify(Integer tenantId, List<Integer> studentIds, String type, Integer referenceId, String title, String body);
}

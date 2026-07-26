package com.edunest.service;

import com.edunest.dto.mobile.StudentDetailResponse;
import com.edunest.dto.mobile.StudentHomeResponse;

public interface MobileStudentService {

    StudentDetailResponse getStudentDetailsById(Integer studentId, Integer tenantId);

    StudentHomeResponse getStudentHome(Integer studentId, Integer tenantId);
}

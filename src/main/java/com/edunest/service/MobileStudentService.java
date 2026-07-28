package com.edunest.service;

import com.edunest.dto.mobile.StudentDetailResponse;
import com.edunest.dto.mobile.StudentExamsResponse;
import com.edunest.dto.mobile.StudentHomeResponse;
import com.edunest.dto.mobile.StudentTimetableResponse;

public interface MobileStudentService {

    StudentDetailResponse getStudentDetailsById(Integer studentId, Integer tenantId);

    StudentHomeResponse getStudentHome(Integer studentId, Integer tenantId);

    StudentTimetableResponse getTimetable(Integer studentId, Integer tenantId, String day);

    StudentExamsResponse getExams(Integer studentId, Integer tenantId);
}

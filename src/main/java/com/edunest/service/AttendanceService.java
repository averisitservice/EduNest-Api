package com.edunest.service;

import com.edunest.dto.attendance.AttendanceRosterResponse;
import com.edunest.dto.attendance.AttendanceSaveRequest;
import com.edunest.dto.attendance.AttendanceSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    AttendanceRosterResponse getAttendanceRoster(Integer tenantId, Integer classId, Integer sectionId, LocalDate date, String search);

    boolean saveAttendance(Integer tenantId, Integer markedBy, AttendanceSaveRequest request);

    List<AttendanceSummaryResponse> getAttendanceReport(Integer tenantId, Integer classId, Integer sectionId, LocalDate fromDate, LocalDate toDate);
}

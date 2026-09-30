package com.edunest.service;

import com.edunest.dto.attendance.AttendanceReportResponse;
import com.edunest.dto.attendance.AttendanceRosterResponse;
import com.edunest.dto.attendance.AttendanceSaveRequest;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    AttendanceRosterResponse getAttendanceRoster(Integer tenantId, Integer classId, Integer sectionId, LocalDate date, String search);

    boolean saveAttendance(Integer tenantId, Integer markedBy, AttendanceSaveRequest request);

    List<AttendanceReportResponse> getAttendanceReport(Integer tenantId, Integer classId, Integer sectionId, LocalDate fromDate, LocalDate toDate);
}

package com.edunest.service;

import com.edunest.dto.leave.LeaveListResponse;
import com.edunest.dto.leave.LeaveRequest;
import com.edunest.dto.leave.LeaveResponse;

import java.util.List;

public interface LeaveService {

    List<LeaveResponse> getLeaveList(Integer tenantId, Integer studentId);

    boolean submitLeave(Integer tenantId, Integer studentId, LeaveRequest request);

    boolean deleteLeave(Integer tenantId, Integer studentId, Integer leaveId);

    List<LeaveListResponse> getLeaveListForClass(Integer tenantId, Integer classId, Integer sectionId);

    boolean updateLeaveStatus(Integer tenantId, Integer teacherId, Integer leaveId, String status);
}

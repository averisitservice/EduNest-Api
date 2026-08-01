package com.edunest.service;

import com.edunest.dto.leave.LeaveRequest;
import com.edunest.dto.leave.LeaveResponse;

import java.util.List;

public interface LeaveService {

    List<LeaveResponse> getLeaveList(Integer tenantId, Integer studentId);

    boolean submitLeave(Integer tenantId, Integer studentId, LeaveRequest request);
}

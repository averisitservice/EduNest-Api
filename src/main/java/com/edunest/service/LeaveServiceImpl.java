package com.edunest.service;

import com.edunest.dto.leave.LeaveRequest;
import com.edunest.dto.leave.LeaveResponse;
import com.edunest.entity.AcademicYear;
import com.edunest.entity.Leave;
import com.edunest.entity.StudentClass;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.LeaveRepository;
import com.edunest.repository.StudentClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeaveServiceImpl implements LeaveService {

    @Autowired
    LeaveRepository leaveRepository;

    @Autowired
    StudentClassRepository studentClassRepository;

    @Autowired
    CommonHelper commonHelper;

    @Override
    public List<LeaveResponse> getLeaveList(Integer tenantId, Integer studentId) {
        List<Leave> leaves = leaveRepository.findByStudentId(tenantId, studentId);

        List<LeaveResponse> leaveResponses = new ArrayList<>();
        for (Leave leave : leaves) {
            LeaveResponse leaveResponse = new LeaveResponse();
            leaveResponse.setLeaveId(leave.getLeaveId());
            leaveResponse.setLeaveDate(leave.getLeaveDate());
            leaveResponse.setReason(leave.getReason());
            leaveResponse.setStatus(leave.getStatus());
            leaveResponse.setCreatedDate(leave.getCreatedDate());
            leaveResponses.add(leaveResponse);
        }
        return leaveResponses;
    }

    @Override
    @Transactional
    public boolean submitLeave(Integer tenantId, Integer studentId, LeaveRequest request) {
        if (request.getLeaveDate() == null) {
            throw new CustomException("leaveDate", "Leave date is required");
        }
        if (!StringUtils.hasText(request.getReason())) {
            throw new CustomException("reason", "Reason is required");
        }

        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        StudentClass studentClass = studentClassRepository.findByStudentIdAndTenantId(studentId, tenantId)
                .orElseThrow(() -> new CustomException("student", "Student is not assigned to a class"));

        Leave leave = new Leave();
        leave.setTenantId(tenantId);
        leave.setAcademicYearId(currentYear.getAcademicYearId());
        leave.setStudentId(studentId);
        leave.setClassId(studentClass.getClassId());
        leave.setSectionId(studentClass.getSectionId());
        leave.setLeaveDate(request.getLeaveDate());
        leave.setReason(request.getReason());
        leave.setStatus("PENDING");
        leaveRepository.save(leave);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteLeave(Integer tenantId, Integer studentId, Integer leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new CustomException("leaveId", "Leave request not found"));

        if (!leave.getTenantId().equals(tenantId) || !leave.getStudentId().equals(studentId)) {
            throw new CustomException("leaveId", "Leave request not found");
        }
        if (!"PENDING".equals(leave.getStatus())) {
            throw new CustomException("status", "Only pending leave requests can be deleted");
        }

        leaveRepository.delete(leave);
        return true;
    }
}

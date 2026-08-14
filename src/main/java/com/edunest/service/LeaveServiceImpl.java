package com.edunest.service;

import com.edunest.constant.Constant;
import com.edunest.dto.leave.LeaveListResponse;
import com.edunest.dto.leave.LeaveRequest;
import com.edunest.dto.leave.LeaveResponse;
import com.edunest.entity.AcademicYear;
import com.edunest.entity.Leave;
import com.edunest.entity.Student;
import com.edunest.entity.StudentClass;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.LeaveRepository;
import com.edunest.repository.StudentClassRepository;
import com.edunest.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeaveServiceImpl implements LeaveService {

    @Autowired
    LeaveRepository leaveRepository;

    @Autowired
    StudentClassRepository studentClassRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    CommonHelper commonHelper;

    @Autowired
    StudentNotificationService studentNotificationService;

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
        leave.setStatus(Constant.LEAVE_STATUS_PENDING);
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
        if (!Constant.LEAVE_STATUS_PENDING.equals(leave.getStatus())) {
            throw new CustomException("status", "Only pending leave requests can be deleted");
        }

        leaveRepository.delete(leave);
        return true;
    }

    @Override
    public List<LeaveListResponse> getLeaveListForClass(Integer tenantId, Integer classId, Integer sectionId) {
        List<Leave> leaves = leaveRepository.findByClassAndSection(tenantId, classId, sectionId);

        List<LeaveListResponse> result = new ArrayList<>();
        for (Leave leave : leaves) {
            Student student = studentRepository.findById(leave.getStudentId()).orElse(null);
            StudentClass studentClass = studentClassRepository
                    .findByStudentIdAndTenantId(leave.getStudentId(), tenantId).orElse(null);

            LeaveListResponse response = new LeaveListResponse();
            response.setLeaveId(leave.getLeaveId());
            response.setStudentId(leave.getStudentId());
            response.setStudentName(CommonHelper.studentNameForStudent(student));
            response.setDisplayClass(commonHelper.displayClassForIds(leave.getClassId(), leave.getSectionId()));
            response.setRollNo(studentClass != null ? studentClass.getRollNo() : null);
            response.setLeaveDate(leave.getLeaveDate());
            response.setReason(leave.getReason());
            response.setStatus(leave.getStatus());
            response.setCreatedDate(leave.getCreatedDate());
            response.setUpdatedBy(commonHelper.teacherNameForId(leave.getUpdatedBy()));
            response.setUpdatedDate(leave.getUpdatedDate());
            result.add(response);
        }
        return result;
    }

    @Override
    @Transactional
    public boolean updateLeaveStatus(Integer tenantId, Integer teacherId, Integer leaveId, String status) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new CustomException("leaveId", "Leave request not found"));

        if (!leave.getTenantId().equals(tenantId)) {
            throw new CustomException("leaveId", "Leave request not found");
        }
        if (!Constant.LEAVE_STATUS_PENDING.equals(leave.getStatus())) {
            throw new CustomException("status", "This leave request has already been actioned");
        }

        leave.setStatus(status.toUpperCase());
        leave.setUpdatedBy(teacherId);
        leave.setUpdatedDate(LocalDateTime.now());
        leaveRepository.save(leave);

        sendLeaveStatusPush(leave);
        return true;
    }

    private void sendLeaveStatusPush(Leave leave) {
        String title = Constant.LEAVE_STATUS_APPROVED.equals(leave.getStatus()) ? "Leave Request Approved" : "Leave Request Rejected";
        String body = "Your leave request for " + leave.getLeaveDate() + " has been " + leave.getStatus().toLowerCase() + ".";

        studentNotificationService.notify(leave.getTenantId(), List.of(leave.getStudentId()),
                Constant.NOTIFICATION_TYPE_LEAVE_STATUS, leave.getLeaveId(), title, body);
    }

}

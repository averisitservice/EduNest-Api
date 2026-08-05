package com.edunest.service;

import com.edunest.constant.Constant;
import com.edunest.dto.attendance.AttendanceRosterResponse;
import com.edunest.dto.attendance.AttendanceSaveRequest;
import com.edunest.dto.attendance.AttendanceSummaryResponse;
import com.edunest.entity.AcademicYear;
import com.edunest.entity.Attendance;
import com.edunest.entity.Leave;
import com.edunest.entity.StudentClass;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.AttendanceRepository;
import com.edunest.repository.LeaveRepository;
import com.edunest.repository.StudentClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    AttendanceRepository attendanceRepository;

    @Autowired
    StudentClassRepository studentClassRepository;

    @Autowired
    LeaveRepository leaveRepository;

    @Autowired
    CommonHelper commonHelper;

    @Override
    public AttendanceRosterResponse getRoster(Integer tenantId, Integer classId, Integer sectionId, LocalDate date) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        List<StudentClass> studentClasses = studentClassRepository.findRoster(classId, sectionId, currentYear.getAcademicYearId(), tenantId);

        List<Integer> studentIds = new ArrayList<>();
        for (StudentClass studentClass : studentClasses) {
            studentIds.add(studentClass.getStudentId());
        }

        Map<Integer, Attendance> existing = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<Attendance> marked = attendanceRepository.findByTenantIdAndAcademicYearIdAndAttendanceDateAndStudentIdIn(
                    tenantId, currentYear.getAcademicYearId(), date, studentIds);
            for (Attendance attendance : marked) {
                existing.put(attendance.getStudentId(), attendance);
            }
        }

        Map<Integer, Leave> approvedLeaves = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<Leave> leaves = leaveRepository.findByTenantIdAndLeaveDateAndStudentIdInAndStatus(
                    tenantId, date, studentIds, "APPROVED");
            for (Leave leave : leaves) {
                approvedLeaves.put(leave.getStudentId(), leave);
            }
        }

        List<AttendanceRosterResponse.StudentRow> rows = new ArrayList<>();
        for (StudentClass studentClass : studentClasses) {
            Attendance attendance = existing.get(studentClass.getStudentId());
            Leave leave = approvedLeaves.get(studentClass.getStudentId());

            AttendanceRosterResponse.StudentRow row = new AttendanceRosterResponse.StudentRow();
            row.setStudentId(studentClass.getStudentId());
            row.setStudentName(commonHelper.studentName(studentClass.getStudentId()));
            row.setRollNo(studentClass.getRollNo());

            if (attendance != null) {
                row.setStatus(attendance.getStatus());
                row.setRemarks(attendance.getRemarks());
            } else if (leave != null) {
                row.setStatus(Constant.LEAVE);
                row.setRemarks("On approved leave: " + leave.getReason());
            }

            row.setOnLeave(leave != null);
            rows.add(row);
        }

        rows.sort(Comparator.comparing(studentRow -> CommonHelper.rollNo(studentRow.getRollNo())));

        AttendanceRosterResponse attendanceRosterResponse = new AttendanceRosterResponse();
        attendanceRosterResponse.setAttendanceDate(date);
        attendanceRosterResponse.setRecords(rows);
        return attendanceRosterResponse;
    }

    @Override
    @Transactional
    public boolean saveAttendance(Integer tenantId, Integer markedBy, AttendanceSaveRequest request) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        if (request.getRecords() == null) {
            return true;
        }

        for (AttendanceSaveRequest.AttendanceItem item : request.getRecords()) {
            if (item.getStudentId() == null || item.getStatus() == null || item.getStatus().isBlank()) {
                continue;
            }

            Attendance attendance = attendanceRepository
                    .findByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDate(
                            tenantId, item.getStudentId(), currentYear.getAcademicYearId(), request.getAttendanceDate())
                    .orElse(new Attendance());

            attendance.setTenantId(tenantId);
            attendance.setStudentId(item.getStudentId());
            attendance.setClassId(request.getClassId());
            attendance.setSectionId(request.getSectionId());
            attendance.setAcademicYearId(currentYear.getAcademicYearId());
            attendance.setAttendanceDate(request.getAttendanceDate());
            attendance.setStatus(item.getStatus());
            attendance.setRemarks(item.getRemarks());
            attendance.setMarkedBy(markedBy);
            attendanceRepository.save(attendance);
        }
        return true;
    }

    @Override
    public List<AttendanceSummaryResponse> getSummary(Integer tenantId, Integer classId, Integer sectionId, LocalDate fromDate, LocalDate toDate) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        List<StudentClass> studentClasses = studentClassRepository.findRoster(classId, sectionId, currentYear.getAcademicYearId(), tenantId);

        List<Integer> studentIds = new ArrayList<>();
        for (StudentClass studentClass : studentClasses) {
            studentIds.add(studentClass.getStudentId());
        }

        Map<Integer, List<Attendance>> byStudent = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<Attendance> attendances = attendanceRepository.findByTenantIdAndAcademicYearIdAndAttendanceDateBetweenAndStudentIdIn(
                    tenantId, currentYear.getAcademicYearId(), fromDate, toDate, studentIds);
            for (Attendance a : attendances) {
                byStudent.computeIfAbsent(a.getStudentId(), k -> new ArrayList<>()).add(a);
            }
        }

        List<AttendanceSummaryResponse> attendanceSummaryResponses = new ArrayList<>();
        for (StudentClass studentClass : studentClasses) {
            List<Attendance> records = byStudent.getOrDefault(studentClass.getStudentId(), new ArrayList<>());

            long present = records.stream().filter(a -> Constant.PRESENT.equals(a.getStatus())).count();
            long absent = records.stream().filter(a -> Constant.ABSENT.equals(a.getStatus())).count();
            long leave = records.stream().filter(a -> Constant.LEAVE.equals(a.getStatus())).count();
            long halfDay = records.stream().filter(a -> Constant.HALFDAY.equals(a.getStatus())).count();
            long total = records.size();

            // Present + Half-day (counted as half) contribute to attendance. Leave counts as an absence.
            double attended = present + (halfDay * 0.5);
            double percentage = total > 0 ? Math.round((attended / total) * 1000.0) / 10.0 : 0.0;

            AttendanceSummaryResponse summary = new AttendanceSummaryResponse();
            summary.setStudentId(studentClass.getStudentId());
            summary.setStudentName(commonHelper.studentName(studentClass.getStudentId()));
            summary.setRollNo(studentClass.getRollNo());
            summary.setPresentCount(present);
            summary.setAbsentCount(absent);
            summary.setLateCount(leave);
            summary.setHalfDayCount(halfDay);
            summary.setTotalMarked(total);
            summary.setPresentPercentage(percentage);
            attendanceSummaryResponses.add(summary);
        }

        attendanceSummaryResponses.sort(Comparator.comparing(r -> CommonHelper.rollNo(r.getRollNo())));
        return attendanceSummaryResponses;
    }
}

package com.edunest.service;

import com.edunest.dto.attendance.AttendanceRosterResponse;
import com.edunest.dto.attendance.AttendanceSaveRequest;
import com.edunest.dto.attendance.AttendanceSummaryResponse;
import com.edunest.entity.AcademicYear;
import com.edunest.entity.Attendance;
import com.edunest.entity.StudentClass;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.AcademicYearRepository;
import com.edunest.repository.AttendanceRepository;
import com.edunest.repository.StudentClassRepository;
import com.edunest.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    AttendanceRepository attendanceRepository;

    @Autowired
    StudentClassRepository studentClassRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    AcademicYearRepository academicYearRepository;

    @Autowired
    CommonHelper commonHelper;

    @Override
    public AttendanceRosterResponse getRoster(Integer tenantId, Integer classId, Integer sectionId, LocalDate date) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        List<StudentClass> roster = studentClassRepository.findRoster(classId, sectionId, currentYear.getAcademicYearId(), tenantId);

        List<Integer> studentIds = new ArrayList<>();
        for (StudentClass sc : roster) {
            studentIds.add(sc.getStudentId());
        }

        Map<Integer, Attendance> existing = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<Attendance> marked = attendanceRepository.findByTenantIdAndAcademicYearIdAndAttendanceDateAndStudentIdIn(
                    tenantId, currentYear.getAcademicYearId(), date, studentIds);
            for (Attendance a : marked) {
                existing.put(a.getStudentId(), a);
            }
        }

        List<AttendanceRosterResponse.StudentRow> rows = new ArrayList<>();
        for (StudentClass sc : roster) {
            Attendance a = existing.get(sc.getStudentId());
            AttendanceRosterResponse.StudentRow row = new AttendanceRosterResponse.StudentRow();
            row.setStudentId(sc.getStudentId());
            row.setStudentName(commonHelper.studentName(sc.getStudentId()));
            row.setRollNo(sc.getRollNo());
            row.setStatus(a != null ? a.getStatus() : null);
            row.setRemarks(a != null ? a.getRemarks() : null);
            rows.add(row);
        }

        rows.sort(Comparator.comparing(r -> CommonHelper.rollNo(r.getRollNo())));

        AttendanceRosterResponse response = new AttendanceRosterResponse();
        response.setAttendanceDate(date);
        response.setRecords(rows);
        return response;
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

        List<StudentClass> roster = studentClassRepository.findRoster(classId, sectionId, currentYear.getAcademicYearId(), tenantId);

        List<Integer> studentIds = new ArrayList<>();
        for (StudentClass sc : roster) {
            studentIds.add(sc.getStudentId());
        }

        Map<Integer, List<Attendance>> byStudent = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<Attendance> records = attendanceRepository.findByTenantIdAndAcademicYearIdAndAttendanceDateBetweenAndStudentIdIn(
                    tenantId, currentYear.getAcademicYearId(), fromDate, toDate, studentIds);
            for (Attendance a : records) {
                byStudent.computeIfAbsent(a.getStudentId(), k -> new ArrayList<>()).add(a);
            }
        }

        List<AttendanceSummaryResponse> result = new ArrayList<>();
        for (StudentClass sc : roster) {
            List<Attendance> records = byStudent.getOrDefault(sc.getStudentId(), new ArrayList<>());

            long present = records.stream().filter(a -> "P".equals(a.getStatus())).count();
            long absent = records.stream().filter(a -> "A".equals(a.getStatus())).count();
            long late = records.stream().filter(a -> "L".equals(a.getStatus())).count();
            long halfDay = records.stream().filter(a -> "H".equals(a.getStatus())).count();
            long total = records.size();

            // Present + Late + Half-day (counted as half) contribute to attendance.
            double attended = present + late + (halfDay * 0.5);
            double percentage = total > 0 ? Math.round((attended / total) * 1000.0) / 10.0 : 0.0;

            AttendanceSummaryResponse summary = new AttendanceSummaryResponse();
            summary.setStudentId(sc.getStudentId());
            summary.setStudentName(commonHelper.studentName(sc.getStudentId()));
            summary.setRollNo(sc.getRollNo());
            summary.setPresentCount(present);
            summary.setAbsentCount(absent);
            summary.setLateCount(late);
            summary.setHalfDayCount(halfDay);
            summary.setTotalMarked(total);
            summary.setPresentPercentage(percentage);
            result.add(summary);
        }

        result.sort(Comparator.comparing(r -> CommonHelper.rollNo(r.getRollNo())));
        return result;
    }
}

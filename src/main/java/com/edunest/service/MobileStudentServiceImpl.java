package com.edunest.service;

import com.edunest.dto.mobile.StudentDetailResponse;
import com.edunest.dto.mobile.StudentHomeResponse;
import com.edunest.entity.AcademicYear;
import com.edunest.entity.ClassMaster;
import com.edunest.entity.ClassSection;
import com.edunest.entity.Student;
import com.edunest.entity.StudentClass;
import com.edunest.entity.Teacher;
import com.edunest.entity.TeacherClass;
import com.edunest.error.CustomException;
import com.edunest.repository.AcademicYearRepository;
import com.edunest.repository.AttendanceRepository;
import com.edunest.repository.ClassMasterRepository;
import com.edunest.repository.ClassSectionRepository;
import com.edunest.repository.StudentClassRepository;
import com.edunest.repository.StudentRepository;
import com.edunest.repository.TeacherClassRepository;
import com.edunest.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MobileStudentServiceImpl implements MobileStudentService {

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    StudentClassRepository studentClassRepository;

    @Autowired
    ClassMasterRepository classMasterRepository;

    @Autowired
    ClassSectionRepository classSectionRepository;

    @Autowired
    TeacherClassRepository teacherClassRepository;

    @Autowired
    TeacherRepository teacherRepository;

    @Autowired
    AttendanceRepository attendanceRepository;

    @Autowired
    AcademicYearRepository academicYearRepository;

    @Override
    public StudentHomeResponse getStudentHome(Integer studentId, Integer tenantId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException("student", "Student not found"));

        if (!student.getTenantId().equals(tenantId)) {
            throw new CustomException("student", "Student not found");
        }

        AcademicYear currentYear = academicYearRepository.findByTenantIdAndIsCurrentTrue(tenantId);
        if (currentYear == null) {
            throw new CustomException("academicYear", "No active academic year found");
        }
        Integer yearId = currentYear.getAcademicYearId();

        StudentHomeResponse response = new StudentHomeResponse();
        response.setStudentId(student.getStudentId());
        response.setStudentName(buildStudentName(student));
        response.setPhotoUrl(student.getPhotoUrl());
        response.setAcademicYearName(currentYear.getYearName());

        StudentClass studentClass = studentClassRepository
                .findByStudentIdAndTenantId(studentId, tenantId)
                .orElse(null);
        if (studentClass != null) {
            ClassMaster classMaster = classMasterRepository.findById(studentClass.getClassId()).orElse(null);
            String className = classMaster != null ? classMaster.getClassName() : null;
            String sectionName = null;
            if (studentClass.getSectionId() != null) {
                ClassSection classSection = classSectionRepository.findById(studentClass.getSectionId()).orElse(null);
                sectionName = classSection != null ? classSection.getSectionName() : null;
            }
            response.setDisplayClass(
                    (className != null && sectionName != null) ? className + " - " + sectionName : className);
            response.setRollNo(studentClass.getRollNo());
        }

        LocalDate today = LocalDate.now();
        response.setTodayStatus(resolveTodayStatus(tenantId, studentId, yearId, today));

        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        long monthPresent = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDateBetweenAndStatus(
                        tenantId, studentId, yearId, monthStart, monthEnd, "P");
        long monthAbsent = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDateBetweenAndStatus(
                        tenantId, studentId, yearId, monthStart, monthEnd, "A");
        long monthLate = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDateBetweenAndStatus(
                        tenantId, studentId, yearId, monthStart, monthEnd, "L");
        long monthTotal = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDateBetween(
                        tenantId, studentId, yearId, monthStart, monthEnd);

        response.setPresentDays(monthPresent);
        response.setAbsentDays(monthAbsent);
        response.setLateDays(monthLate);
        response.setThisMonthPercent(percent(monthPresent + monthLate, monthTotal));

        long yearPresent = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearIdAndStatus(tenantId, studentId, yearId, "P");
        long yearLate = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearIdAndStatus(tenantId, studentId, yearId, "L");
        long yearTotal = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearId(tenantId, studentId, yearId);

        response.setAveragePercent(percent(yearPresent + yearLate, yearTotal));

        return response;
    }

    private String resolveTodayStatus(Integer tenantId, Integer studentId, Integer yearId, LocalDate today) {
        return attendanceRepository
                .findByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDate(tenantId, studentId, yearId, today)
                .map(attendance -> switch (attendance.getStatus()) {
                    case "P" -> "PRESENT";
                    case "A" -> "ABSENT";
                    case "L" -> "LATE";
                    default -> "NOT_MARKED";
                })
                .orElse("NOT_MARKED");
    }

    private double percent(long attended, long total) {
        if (total <= 0) {
            return 0.0;
        }
        return Math.round((attended * 10000.0 / total)) / 100.0;
    }

    @Override
    public StudentDetailResponse getStudentDetailsById(Integer studentId, Integer tenantId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException("studentId", "Student not found"));

        if (!student.getTenantId().equals(tenantId)) {
            throw new CustomException("studentId", "Student not found");
        }

        StudentDetailResponse response = new StudentDetailResponse();
        response.setStudentId(student.getStudentId());
        response.setAdmissionNo(student.getAdmissionNo());
        response.setUsername(student.getUsername());
        response.setStudentName(buildStudentName(student));
        response.setPhotoUrl(student.getPhotoUrl());

        response.setDateOfBirth(student.getDateOfBirth());
        response.setGender(student.getGender() != null ? String.valueOf(student.getGender()) : null);
        response.setAadharNo(student.getAadharNo());
        response.setEmail(student.getEmail());
        response.setMobileNo(student.getMobileNo());
        response.setIsHostel(student.getIsHostel());

        response.setFatherName(student.getFatherName());
        response.setMotherName(student.getMotherName());
        response.setParentMobile(student.getParentMobile());
        response.setParentEmail(student.getParentEmail());
        response.setParentAadhar(student.getParentAadhar());

        response.setAddress(buildFullAddress(student));

        applyClassPlacement(response, student, tenantId);

        return response;
    }

    private void applyClassPlacement(StudentDetailResponse response, Student student, Integer tenantId) {
        StudentClass studentClass = studentClassRepository
                .findByStudentIdAndTenantId(student.getStudentId(), tenantId)
                .orElse(null);

        if (studentClass == null) {
            return;
        }

        ClassMaster classMaster = classMasterRepository.findById(studentClass.getClassId()).orElse(null);
        String className = classMaster != null ? classMaster.getClassName() : null;

        String sectionName = null;
        if (studentClass.getSectionId() != null) {
            ClassSection classSection = classSectionRepository.findById(studentClass.getSectionId()).orElse(null);
            sectionName = classSection != null ? classSection.getSectionName() : null;
        }

        response.setClassId(studentClass.getClassId());
        response.setClassName(className);
        response.setSectionId(studentClass.getSectionId());
        response.setSectionName(sectionName);
        response.setDisplayClass(
                (className != null && sectionName != null) ? className + " - " + sectionName : className);
        response.setRollNo(studentClass.getRollNo());
        response.setClassTeacherName(
                resolveClassTeacher(studentClass.getClassId(), studentClass.getSectionId(), tenantId));
    }

    private String resolveClassTeacher(Integer classId, Integer sectionId, Integer tenantId) {
        List<TeacherClass> assignments = teacherClassRepository
                .findByClassIdAndSectionIdAndTenantIdAndIsActiveTrue(classId, sectionId, tenantId);

        if (assignments.isEmpty()) {
            return null;
        }

        Teacher teacher = teacherRepository.findById(assignments.get(0).getTeacherId()).orElse(null);
        if (teacher == null) {
            return null;
        }

        String name = ((teacher.getFirstName() != null ? teacher.getFirstName() : "") + " "
                + (teacher.getLastName() != null ? teacher.getLastName() : "")).trim();

        return name.isEmpty() ? teacher.getTeacherName() : name;
    }

    private String buildFullAddress(Student student) {
        List<String> parts = new ArrayList<>();

        if (hasText(student.getAddressLine1())) parts.add(student.getAddressLine1().trim());
        if (hasText(student.getCity())) parts.add(student.getCity().trim());
        if (hasText(student.getState())) parts.add(student.getState().trim());

        String address = String.join(", ", parts);

        if (hasText(student.getPostalCode())) {
            address = address.isEmpty()
                    ? student.getPostalCode().trim()
                    : address + " - " + student.getPostalCode().trim();
        }

        return address.isEmpty() ? null : address;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String buildStudentName(Student student) {
        StringBuilder name = new StringBuilder();
        if (student.getFirstName() != null) name.append(student.getFirstName());
        if (student.getLastName() != null) name.append(" ").append(student.getLastName());
        return name.toString().trim();
    }
}

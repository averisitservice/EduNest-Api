package com.edunest.repository;

import com.edunest.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    Optional<Attendance> findByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDate(
            Integer tenantId, Integer studentId, Integer academicYearId, LocalDate attendanceDate);

    List<Attendance> findByTenantIdAndAcademicYearIdAndAttendanceDateAndStudentIdIn(
            Integer tenantId, Integer academicYearId, LocalDate attendanceDate, List<Integer> studentIds);

    List<Attendance> findByTenantIdAndAcademicYearIdAndAttendanceDateBetweenAndStudentIdIn(
            Integer tenantId, Integer academicYearId, LocalDate fromDate, LocalDate toDate, List<Integer> studentIds);

    long countByTenantIdAndAcademicYearIdAndAttendanceDate(
            Integer tenantId, Integer academicYearId, LocalDate attendanceDate);

    long countByTenantIdAndAcademicYearIdAndAttendanceDateAndStatus(
            Integer tenantId, Integer academicYearId, LocalDate attendanceDate, String status);

    long countByTenantIdAndStudentIdAndAcademicYearId(
            Integer tenantId, Integer studentId, Integer academicYearId);

    long countByTenantIdAndStudentIdAndAcademicYearIdAndStatus(
            Integer tenantId, Integer studentId, Integer academicYearId, String status);

    long countByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDateBetween(
            Integer tenantId, Integer studentId, Integer academicYearId, LocalDate fromDate, LocalDate toDate);

    long countByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDateBetweenAndStatus(
            Integer tenantId, Integer studentId, Integer academicYearId,
            LocalDate fromDate, LocalDate toDate, String status);
}

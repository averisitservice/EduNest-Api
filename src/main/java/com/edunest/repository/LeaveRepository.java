package com.edunest.repository;

import com.edunest.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Integer> {

    @Query("SELECT l FROM Leave l WHERE l.tenantId = :tenantId AND l.academicYearId = :academicYearId "
            + "AND l.studentId = :studentId "
            + "ORDER BY l.leaveId DESC")
    List<Leave> findByStudentId(
            @Param("tenantId") Integer tenantId, @Param("academicYearId") Integer academicYearId,
            @Param("studentId") Integer studentId);

    @Query("SELECT l FROM Leave l WHERE l.tenantId = :tenantId AND l.academicYearId = :academicYearId "
            + "AND l.classId = :classId "
            + "AND (:sectionId IS NULL OR l.sectionId = :sectionId) "
            + "ORDER BY l.leaveId DESC")
    List<Leave> findByClassAndSection(
            @Param("tenantId") Integer tenantId, @Param("academicYearId") Integer academicYearId,
            @Param("classId") Integer classId, @Param("sectionId") Integer sectionId);

    List<Leave> findByTenantIdAndLeaveDateAndStudentIdInAndStatus(
            Integer tenantId, LocalDate leaveDate, List<Integer> studentIds, String status);

    List<Leave> findByTenantIdAndStudentIdAndLeaveDateBetweenAndStatus(
            Integer tenantId, Integer studentId, LocalDate fromDate, LocalDate toDate, String status);
}

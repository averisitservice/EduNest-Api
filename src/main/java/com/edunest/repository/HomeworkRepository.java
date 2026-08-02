package com.edunest.repository;

import com.edunest.entity.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Integer> {

    @Query("SELECT h FROM Homework h WHERE h.tenantId = :tenantId AND h.academicYearId = :academicYearId "
            + "AND h.isActive = true AND h.classId = :classId "
            + "AND ((:sectionId IS NULL AND h.sectionId IS NULL) OR h.sectionId = :sectionId) "
            + "ORDER BY h.homeworkId DESC")
    List<Homework> findList(
            @Param("tenantId") Integer tenantId, @Param("academicYearId") Integer academicYearId,
            @Param("classId") Integer classId, @Param("sectionId") Integer sectionId);

    @Query("SELECT h FROM Homework h WHERE h.tenantId = :tenantId AND h.academicYearId = :academicYearId "
            + "AND h.isActive = true AND h.classId = :classId "
            + "AND (h.sectionId IS NULL OR h.sectionId = :sectionId) "
            + "AND h.dueDate >= COALESCE(:fromDate, h.dueDate) "
            + "AND h.dueDate <= COALESCE(:toDate, h.dueDate) "
            + "ORDER BY h.homeworkId DESC")
    List<Homework> findHomeForStudentInDateRange(
            @Param("tenantId") Integer tenantId, @Param("academicYearId") Integer academicYearId,
            @Param("classId") Integer classId, @Param("sectionId") Integer sectionId,
            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);
}

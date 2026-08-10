package com.edunest.repository;

import com.edunest.entity.StudentClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentClassRepository extends JpaRepository<StudentClass, Integer> {

    Optional<StudentClass> findByStudentIdAndTenantId(Integer studentId, Integer tenantId);

    boolean existsBySectionIdAndTenantId(Integer sectionId, Integer tenantId);

    @Query("SELECT sc FROM StudentClass sc WHERE sc.tenantId = :tenantId AND sc.classId = :classId "
            + "AND sc.academicYearId = :academicYearId AND sc.isActive = true "
            + "AND ((:sectionId IS NULL AND sc.sectionId IS NULL) OR sc.sectionId = :sectionId)")
    List<StudentClass> findRoster(
            @Param("classId") Integer classId, @Param("sectionId") Integer sectionId,
            @Param("academicYearId") Integer academicYearId, @Param("tenantId") Integer tenantId);

    @Query("SELECT DISTINCT sc.studentId FROM StudentClass sc WHERE sc.tenantId = :tenantId "
            + "AND sc.academicYearId = :academicYearId AND sc.isActive = true AND sc.classId IN :classIds")
    List<Integer> findStudentIdsByClassIds(
            @Param("tenantId") Integer tenantId, @Param("academicYearId") Integer academicYearId,
            @Param("classIds") List<Integer> classIds);
}
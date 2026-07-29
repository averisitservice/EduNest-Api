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

    List<StudentClass> findByClassIdAndSectionIdAndAcademicYearIdAndTenantId(
            Integer classId, Integer sectionId,
            Integer academicYearId, Integer tenantId);

    @Query("SELECT sc FROM StudentClass sc WHERE sc.tenantId = :tenantId AND sc.classId = :classId "
            + "AND sc.academicYearId = :academicYearId AND sc.isActive = true "
            + "AND ((:sectionId IS NULL AND sc.sectionId IS NULL) OR sc.sectionId = :sectionId)")
    List<StudentClass> findRoster(
            @Param("classId") Integer classId, @Param("sectionId") Integer sectionId,
            @Param("academicYearId") Integer academicYearId, @Param("tenantId") Integer tenantId);
}
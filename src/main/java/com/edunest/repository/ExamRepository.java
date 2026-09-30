package com.edunest.repository;

import com.edunest.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Integer> {

    List<Exam> findByTenantIdAndAcademicYearIdAndClassIdAndIsActiveTrueOrderByExamIdDesc(Integer tenantId, Integer academicYearId, Integer classId);

    List<Exam> findByTenantIdAndAcademicYearIdAndIsActiveTrueOrderByExamIdDesc(Integer tenantId, Integer academicYearId);
}

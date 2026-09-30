package com.edunest.repository;

import com.edunest.entity.ExamMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamMarkRepository extends JpaRepository<ExamMark, Integer> {

    List<ExamMark> findByTenantIdAndExamIdAndStudentIdIn(Integer tenantId, Integer examId, List<Integer> studentIds);

    List<ExamMark> findByTenantIdAndExamIdAndStudentId(Integer tenantId, Integer examId, Integer studentId);

    Optional<ExamMark> findByTenantIdAndExamIdAndStudentIdAndSubjectId(Integer tenantId, Integer examId, Integer studentId, Integer subjectId);
}

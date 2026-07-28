package com.edunest.repository;

import com.edunest.entity.ExamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Integer> {

    List<ExamSchedule> findByExamIdAndTenantIdOrderByExamDateAscExamScheduleIdAsc(Integer examId, Integer tenantId);

    void deleteByExamIdAndTenantId(Integer examId, Integer tenantId);
}

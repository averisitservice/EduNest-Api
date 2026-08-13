package com.edunest.repository;

import com.edunest.entity.StudentNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentNotificationRepository extends JpaRepository<StudentNotification, Integer> {

    Page<StudentNotification> findByTenantIdAndStudentIdOrderByUpdatedDateDescStudentNotificationIdDesc(
            Integer tenantId, Integer studentId, Pageable pageable);

    long countByTenantIdAndStudentIdAndIsReadFalse(Integer tenantId, Integer studentId);

    List<StudentNotification> findByTenantIdAndTypeAndReferenceIdAndStudentIdIn(
            Integer tenantId, String type, Integer referenceId, List<Integer> studentIds);
}

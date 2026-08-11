package com.edunest.repository;

import com.edunest.entity.StudentNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentNotificationRepository extends JpaRepository<StudentNotification, Integer> {

    Page<StudentNotification> findByTenantIdAndStudentIdOrderByCreatedDateDescStudentNotificationIdDesc(
            Integer tenantId, Integer studentId, Pageable pageable);
}

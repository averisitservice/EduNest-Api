package com.edunest.repository;

import com.edunest.entity.StudentBulkUploadError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentBulkUploadErrorRepository extends JpaRepository<StudentBulkUploadError, Integer> {

    List<StudentBulkUploadError> findByUploadIdAndTenantIdOrderByExcelRowNumberAsc(Integer uploadId, Integer tenantId);

    long countByUploadIdAndTenantId(Integer uploadId, Integer tenantId);
}

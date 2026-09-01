package com.edunest.repository;

import com.edunest.entity.StudentBulkUpload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentBulkUploadRepository extends JpaRepository<StudentBulkUpload, Integer> {
    
    Optional<StudentBulkUpload> findByUploadIdAndTenantId(Integer uploadId, Integer tenantId);

    Page<StudentBulkUpload> findByTenantIdOrderByUploadIdDesc(Integer tenantId, Pageable pageable);
}

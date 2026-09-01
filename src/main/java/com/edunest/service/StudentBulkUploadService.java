package com.edunest.service;

import com.edunest.common.PagedResponse;
import com.edunest.dto.student.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StudentBulkUploadService {

    StudentBulkUploadValidationResponse validateBulkUpload(Integer tenantId, Integer loginTeacherId, MultipartFile file);

    StudentBulkUploadInsertResponse insertBulkUpload(Integer tenantId, Integer loginTeacherId, Integer uploadId);

    PagedResponse<StudentBulkUploadHistoryResponse> getUploadHistory(Integer tenantId, int page, int size);

    List<StudentUploadErrorDTO> getUploadErrors(Integer tenantId, Integer uploadId);
}

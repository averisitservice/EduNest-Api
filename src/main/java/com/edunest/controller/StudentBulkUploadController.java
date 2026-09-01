package com.edunest.controller;

import com.edunest.common.PagedResponse;
import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.student.*;
import com.edunest.service.StudentBulkUploadService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/student/bulk-upload")
public class StudentBulkUploadController {

    @Autowired
    StudentBulkUploadService bulkUploadService;

    @Autowired
    JwtHelper jwtHelper;

    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject<StudentBulkUploadValidationResponse>> validateBulkUpload(
            HttpServletRequest request,
            @RequestPart("file") MultipartFile file) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer tenantId = jwtHelper.extractTenantId(token);
        Integer loginTeacherId = jwtHelper.extractTeacherId(token);

        ResponseObject<StudentBulkUploadValidationResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(bulkUploadService.validateBulkUpload(tenantId, loginTeacherId, file));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/insert/{uploadId}")
    public ResponseEntity<ResponseObject<StudentBulkUploadInsertResponse>> insertBulkUpload(
            HttpServletRequest request,
            @PathVariable Integer uploadId) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer tenantId = jwtHelper.extractTenantId(token);
        Integer loginTeacherId = jwtHelper.extractTeacherId(token);

        ResponseObject<StudentBulkUploadInsertResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(bulkUploadService.insertBulkUpload(tenantId, loginTeacherId, uploadId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<ResponseObject<PagedResponse<StudentBulkUploadHistoryResponse>>> getUploadHistory(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<PagedResponse<StudentBulkUploadHistoryResponse>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(bulkUploadService.getUploadHistory(tenantId, page, size));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/errors/{uploadId}")
    public ResponseEntity<ResponseObject<List<StudentUploadErrorDTO>>> getUploadErrors(
            HttpServletRequest request,
            @PathVariable Integer uploadId) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<List<StudentUploadErrorDTO>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(bulkUploadService.getUploadErrors(tenantId, uploadId));
        return ResponseEntity.ok(response);
    }
}

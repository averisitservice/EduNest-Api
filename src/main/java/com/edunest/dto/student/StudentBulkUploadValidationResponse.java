package com.edunest.dto.student;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentBulkUploadValidationResponse {
    private Integer uploadId;
    private String fileName;
    private Integer totalRows;
    private Integer validRows;
    private Integer failedRows;
    private String status;
    private LocalDateTime createdDate;
    private List<StudentUploadErrorDTO> errors;
}

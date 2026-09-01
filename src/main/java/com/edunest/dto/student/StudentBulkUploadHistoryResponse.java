package com.edunest.dto.student;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentBulkUploadHistoryResponse {
    private Integer uploadId;
    private String fileName;
    private Integer totalRows;
    private Integer successRows;
    private Integer failedRows;
    private Integer insertedRows;
    private String status;
    private String uploadedByName;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}

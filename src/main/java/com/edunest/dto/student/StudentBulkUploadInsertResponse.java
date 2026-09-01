package com.edunest.dto.student;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentBulkUploadInsertResponse {
    private Integer uploadId;
    private Integer totalRows;
    private Integer insertedRows;
    private Integer skippedRows;
    private Integer failedRows;
    private String status;
    private String message;
}

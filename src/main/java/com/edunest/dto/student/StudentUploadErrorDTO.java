package com.edunest.dto.student;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentUploadErrorDTO {
    private Integer errorId;
    private Integer excelRowNumber;
    private String studentIdentifier;
    private String errorType;
    private String errorReason;
    private String rowData;
    private String status;
}

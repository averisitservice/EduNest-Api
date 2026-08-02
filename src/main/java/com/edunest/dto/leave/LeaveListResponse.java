package com.edunest.dto.leave;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveListResponse {
    private Integer leaveId;
    private Integer studentId;
    private String studentName;
    private String displayClass;
    private String rollNo;
    private LocalDate leaveDate;
    private String reason;
    private String status;
    private LocalDateTime createdDate;
    private String updatedBy;
    private LocalDateTime updatedDate;
}

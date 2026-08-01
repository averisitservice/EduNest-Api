package com.edunest.dto.leave;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveResponse {
    private Integer leaveId;
    private LocalDate leaveDate;
    private String reason;
    private String status;
    private LocalDateTime createdDate;
}

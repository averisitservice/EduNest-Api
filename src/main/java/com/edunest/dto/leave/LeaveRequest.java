package com.edunest.dto.leave;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {
    private LocalDate leaveDate;
    private String reason;
}

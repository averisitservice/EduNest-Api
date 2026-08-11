package com.edunest.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentNotificationItem {

    private Integer notificationId;
    private String type;
    private Integer referenceId;
    private String title;
    private String body;
    private Boolean isRead;
    private LocalDateTime createdDate;
}

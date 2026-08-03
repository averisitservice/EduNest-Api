package com.edunest.dto.announcement;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementResponse {
    private Integer announcementId;
    private String title;
    private String message;
    private String audience;
    private List<Integer> classIds;
    private List<String> classNames;
    private LocalDateTime publishDate;
    private String status;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime updatedDate;
}

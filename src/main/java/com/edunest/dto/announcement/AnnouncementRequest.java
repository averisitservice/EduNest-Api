package com.edunest.dto.announcement;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementRequest {
    private Integer announcementId;
    private String title;
    private String message;
    private String audience;
    private List<Integer> classIds;
    private String publishMode;
    private LocalDateTime publishDate;
}

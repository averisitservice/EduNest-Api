package com.edunest.dto.homework;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkResponse {
    private Integer homeworkId;
    private Integer classId;
    private Integer sectionId;
    private Integer subjectId;
    private String subjectName;
    private String title;
    private String description;
    private LocalDate dueDate;
    private String attachmentUrl;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime updatedDate;
}

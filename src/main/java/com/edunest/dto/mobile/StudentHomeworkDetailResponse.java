package com.edunest.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentHomeworkDetailResponse {
    private Integer homeworkId;
    private Integer subjectId;
    private String subjectName;
    private String title;
    private String description;
    private LocalDate dueDate;
    private String attachmentUrl;
    private String teacherName;
    private LocalDateTime updatedDate;
}

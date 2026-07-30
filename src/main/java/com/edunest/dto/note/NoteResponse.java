package com.edunest.dto.note;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponse {
    private Integer noteId;
    private Integer classId;
    private Integer sectionId;
    private Integer subjectId;
    private String subjectName;
    private String title;
    private String description;
    private String attachmentUrl;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime updatedDate;
}

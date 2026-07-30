package com.edunest.dto.note;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoteRequest {
    private Integer noteId;
    private Integer classId;
    private Integer sectionId;
    private Integer subjectId;
    private String title;
    private String description;
    private String attachmentUrl;
}

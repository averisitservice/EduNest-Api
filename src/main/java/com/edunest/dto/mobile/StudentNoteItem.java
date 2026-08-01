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
public class StudentNoteItem {
    private Integer noteId;
    private String subjectName;
    private String title;
    private LocalDateTime updatedDate;
}

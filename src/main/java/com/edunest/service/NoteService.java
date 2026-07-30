package com.edunest.service;

import com.edunest.dto.note.NoteRequest;
import com.edunest.dto.note.NoteResponse;

import java.util.List;

public interface NoteService {

    List<NoteResponse> getNoteList(Integer tenantId, Integer classId, Integer sectionId);

    boolean saveNote(Integer tenantId, Integer loginTeacherId, NoteRequest request);

    boolean deleteNote(Integer tenantId, Integer noteId);
}

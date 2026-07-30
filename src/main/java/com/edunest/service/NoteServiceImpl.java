package com.edunest.service;

import com.edunest.dto.note.NoteRequest;
import com.edunest.dto.note.NoteResponse;
import com.edunest.entity.AcademicYear;
import com.edunest.entity.Note;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.AcademicYearRepository;
import com.edunest.repository.NoteRepository;
import com.edunest.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {

    @Autowired
    NoteRepository noteRepository;

    @Autowired
    TeacherRepository teacherRepository;

    @Autowired
    AcademicYearRepository academicYearRepository;

    @Autowired
    CommonHelper commonHelper;

    @Override
    public List<NoteResponse> getNoteList(Integer tenantId, Integer classId, Integer sectionId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        List<Note> items = noteRepository.findList(tenantId, currentYear.getAcademicYearId(), classId, sectionId);

        List<NoteResponse> result = new ArrayList<>();
        for (Note n : items) {
            NoteResponse response = new NoteResponse();
            response.setNoteId(n.getNoteId());
            response.setClassId(n.getClassId());
            response.setSectionId(n.getSectionId());
            response.setSubjectId(n.getSubjectId());
            response.setSubjectName(commonHelper.subjectName(n.getSubjectId()));
            response.setTitle(n.getTitle());
            response.setDescription(n.getDescription());
            response.setAttachmentUrl(n.getAttachmentUrl());
            response.setCreatedBy(commonHelper.teacherName(n.getCreatedBy()));
            response.setUpdatedBy(commonHelper.teacherName(n.getUpdatedBy()));
            response.setUpdatedDate(n.getUpdatedDate());
            result.add(response);
        }
        return result;
    }

    @Override
    @Transactional
    public boolean saveNote(Integer tenantId, Integer loginTeacherId, NoteRequest request) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        if (request.getClassId() == null) {
            throw new CustomException("classId", "Class is required");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new CustomException("title", "Title is required");
        }

        Note note;
        if (request.getNoteId() != null) {
            note = noteRepository.findById(request.getNoteId())
                    .orElseThrow(() -> new CustomException("noteId", "Item not found"));
        } else {
            note = new Note();
            note.setTenantId(tenantId);
            note.setAcademicYearId(currentYear.getAcademicYearId());
            note.setIsActive(true);
            note.setCreatedBy(loginTeacherId);
        }

        note.setClassId(request.getClassId());
        note.setSectionId(request.getSectionId());
        note.setSubjectId(request.getSubjectId());
        note.setTitle(request.getTitle());
        note.setDescription(request.getDescription());
        note.setAttachmentUrl(request.getAttachmentUrl());
        note.setUpdatedBy(loginTeacherId);
        note.setUpdatedDate(LocalDateTime.now());
        noteRepository.save(note);
        return true;
    }

    @Override
    public boolean deleteNote(Integer tenantId, Integer noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new CustomException("noteId", "Item not found"));
        note.setIsActive(false);
        noteRepository.save(note);
        return true;
    }
}

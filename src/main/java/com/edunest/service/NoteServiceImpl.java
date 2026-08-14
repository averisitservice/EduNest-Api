package com.edunest.service;

import com.edunest.dto.note.NoteRequest;
import com.edunest.dto.note.NoteResponse;
import com.edunest.entity.AcademicYear;
import com.edunest.entity.Note;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.NoteRepository;
import com.edunest.repository.StudentClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NoteServiceImpl implements NoteService {

    private static final String ATTACHMENT_FOLDER = "edunest/note";

    @Autowired
    NoteRepository noteRepository;

    @Autowired
    CommonHelper commonHelper;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    StudentClassRepository studentClassRepository;

    @Autowired
    StudentNotificationService studentNotificationService;

    @Override
    public List<NoteResponse> getNoteList(Integer tenantId, Integer classId, Integer sectionId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        List<Note> notes = noteRepository.findList(tenantId, currentYear.getAcademicYearId(), classId, sectionId);

        List<NoteResponse> noteResponses = new ArrayList<>();
        for (Note note : notes) {
            NoteResponse noteResponse = new NoteResponse();
            noteResponse.setNoteId(note.getNoteId());
            noteResponse.setClassId(note.getClassId());
            noteResponse.setSectionId(note.getSectionId());
            noteResponse.setSubjectId(note.getSubjectId());
            noteResponse.setSubjectName(commonHelper.subjectName(note.getSubjectId()));
            noteResponse.setTitle(note.getTitle());
            noteResponse.setDescription(note.getDescription());
            noteResponse.setAttachmentUrl(note.getAttachmentUrl());
            noteResponse.setCreatedBy(commonHelper.teacherNameForId(note.getCreatedBy()));
            noteResponse.setUpdatedBy(commonHelper.teacherNameForId(note.getUpdatedBy()));
            noteResponse.setUpdatedDate(note.getUpdatedDate());
            noteResponses.add(noteResponse);
        }
        return noteResponses;
    }

    @Override
    @Transactional
    public boolean saveNote(Integer tenantId, Integer loginTeacherId, NoteRequest request, MultipartFile file) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        boolean isNew = request.getNoteId() == null;

        Note note;
        if (!isNew) {
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

        if (file != null && !file.isEmpty()) {
            Map<String, Object> uploadResult = fileStorageService.uploadFile(file, ATTACHMENT_FOLDER);
            note.setAttachmentUrl(String.valueOf(uploadResult.get("secure_url")));
        } else if (request.getNoteId() == null) {
            note.setAttachmentUrl(request.getAttachmentUrl());
        }

        note.setUpdatedBy(loginTeacherId);
        note.setUpdatedDate(LocalDateTime.now());
        noteRepository.save(note);

        sendNotePush(note, isNew);
        return true;
    }

    private void sendNotePush(Note note, boolean isNew) {
        List<Integer> studentIds = studentClassRepository.findStudentIdsByClassAndSection(
                note.getTenantId(), note.getAcademicYearId(), note.getClassId(), note.getSectionId());
        if (studentIds.isEmpty()) {
            return;
        }

        String subjectName = commonHelper.subjectName(note.getSubjectId());
        String title = (isNew ? "New Note: " : "Note Updated: ") + note.getTitle();
        studentNotificationService.notify(note.getTenantId(), studentIds, "NOTE", note.getNoteId(),
                title, subjectName != null ? subjectName : note.getTitle());
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

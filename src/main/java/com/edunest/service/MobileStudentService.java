package com.edunest.service;

import com.edunest.dto.mobile.StudentDetailResponse;
import com.edunest.dto.mobile.StudentExamsResponse;
import com.edunest.dto.mobile.StudentHomeResponse;
import com.edunest.dto.mobile.StudentHomeworkDetailResponse;
import com.edunest.dto.mobile.StudentHomeworkItem;
import com.edunest.dto.mobile.StudentNoteDetailResponse;
import com.edunest.dto.mobile.StudentNoteItem;
import com.edunest.dto.mobile.StudentTimetableResponse;

import java.time.LocalDate;
import java.util.List;

public interface MobileStudentService {

    StudentDetailResponse getStudentDetailsById(Integer studentId, Integer tenantId);

    StudentHomeResponse getStudentHome(Integer studentId, Integer tenantId);

    StudentTimetableResponse getTimetable(Integer studentId, Integer tenantId, String day);

    StudentExamsResponse getExams(Integer studentId, Integer tenantId);

    List<StudentHomeworkItem> getHomework(Integer studentId, Integer tenantId, LocalDate fromDate, LocalDate toDate);

    List<StudentNoteItem> getNotes(Integer studentId, Integer tenantId);

    StudentHomeworkDetailResponse getHomeworkDetail(Integer studentId, Integer tenantId, Integer homeworkId);

    StudentNoteDetailResponse getNoteDetail(Integer studentId, Integer tenantId, Integer noteId);
}

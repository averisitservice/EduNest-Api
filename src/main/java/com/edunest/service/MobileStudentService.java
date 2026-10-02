package com.edunest.service;

import com.edunest.common.PagedResponse;
import com.edunest.dto.exam.ReportCardResponse;
import com.edunest.dto.mobile.*;

import java.time.LocalDate;
import java.util.List;

public interface MobileStudentService {

    StudentHomeResponse getStudentHome(Integer studentId, Integer tenantId);

    StudentTimetableResponse getTimetable(Integer studentId, Integer tenantId, String day);

    StudentExamsResponse getExams(Integer studentId, Integer tenantId);

    List<StudentHomeworkItem> getHomework(Integer studentId, Integer tenantId, LocalDate fromDate, LocalDate toDate);

    List<StudentNoteItem> getNotes(Integer studentId, Integer tenantId, LocalDate fromDate, LocalDate toDate);

    StudentAttendanceResponse getAttendance(Integer studentId, Integer tenantId, LocalDate fromDate, LocalDate toDate);

    StudentHomeworkDetailResponse getHomeworkDetail(Integer studentId, Integer tenantId, Integer homeworkId);

    StudentNoteDetailResponse getNoteDetail(Integer studentId, Integer tenantId, Integer noteId);

    StudentResultsResponse getResults(Integer studentId, Integer tenantId);

    ReportCardResponse getResultDetail(Integer studentId, Integer tenantId, Integer examId);

    PagedResponse<StudentNotificationItem> getNotifications(Integer studentId, Integer tenantId, int page, int size);

    boolean markNotificationAsRead(Integer studentId, Integer tenantId, Integer notificationId);

    List<StudentAnnouncementItem> getAnnouncements(Integer studentId, Integer tenantId);

    StudentDetailResponse getStudentDetailsById(Integer studentId, Integer tenantId);
}

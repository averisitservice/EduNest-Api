package com.edunest.service;

import com.edunest.dto.exam.ExamListResponse;
import com.edunest.dto.exam.ExamMarksEntryResponse;
import com.edunest.dto.exam.ExamMarksSaveRequest;
import com.edunest.dto.exam.ExamRequest;
import com.edunest.dto.exam.ReportCardResponse;

import java.util.List;

public interface ExamService {

    List<ExamListResponse> getExams(Integer tenantId, Integer classId);

    ExamListResponse getExamById(Integer tenantId, Integer examId);

    boolean saveExam(Integer tenantId, Integer loginTeacherId, ExamRequest request);

    boolean deleteExam(Integer tenantId, Integer examId);

    ExamMarksEntryResponse getMarksEntry(Integer tenantId, Integer examId, Integer classId, Integer sectionId);

    boolean saveMarks(Integer tenantId, ExamMarksSaveRequest request);

    ReportCardResponse getReportCard(Integer tenantId, Integer examId, Integer studentId);
}

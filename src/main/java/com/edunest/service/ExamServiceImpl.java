package com.edunest.service;

import com.edunest.dto.exam.*;
import com.edunest.entity.*;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    ExamRepository examRepository;

    @Autowired
    ExamMarkRepository examMarkRepository;

    @Autowired
    StudentClassRepository studentClassRepository;

    @Autowired
    ClassSubjectRepository classSubjectRepository;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    ExamScheduleRepository examScheduleRepository;

    @Autowired
    ClassMasterRepository classMasterRepository;

    @Autowired
    CommonHelper commonHelper;

    private List<Subject> classSubjects(Integer classId, Integer tenantId) {
        List<ClassSubject> classSubjects = classSubjectRepository.findByClassIdAndTenantId(classId, tenantId);
        List<Subject> subjects = new ArrayList<>();
        for (ClassSubject classSubject : classSubjects) {
            if (Boolean.FALSE.equals(classSubject.getIsActive())) continue;
            Subject subject = subjectRepository.findById(classSubject.getSubjectId()).orElse(null);
            if (subject != null && Boolean.TRUE.equals(subject.getIsActive())) {
                subjects.add(subject);
            }
        }
        return subjects;
    }

    private String gradeFor(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B";
        if (percentage >= 60) return "C";
        if (percentage >= 50) return "D";
        if (percentage >= 40) return "E";
        return "F";
    }

    @Override
    public List<ExamSummaryResponse> getExams(Integer tenantId, Integer classId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        List<Exam> exams = classId != null
                ? examRepository.findByTenantIdAndAcademicYearIdAndClassIdAndIsActiveTrueOrderByExamIdDesc(tenantId, currentYear.getAcademicYearId(), classId)
                : examRepository.findByTenantIdAndAcademicYearIdAndIsActiveTrueOrderByExamIdDesc(tenantId, currentYear.getAcademicYearId());

        List<ExamSummaryResponse> examSummaryResponses = new ArrayList<>();
        for (Exam exam : exams) {
            ClassMaster classMaster = classMasterRepository.findById(exam.getClassId()).orElse(null);

            ExamSummaryResponse examSummaryResponse = new ExamSummaryResponse();
            examSummaryResponse.setExamId(exam.getExamId());
            examSummaryResponse.setClassId(exam.getClassId());
            examSummaryResponse.setClassName(classMaster != null ? classMaster.getClassName() : null);
            examSummaryResponse.setExamName(exam.getExamName());
            examSummaryResponse.setExamDate(exam.getExamDate());
            List<ExamScheduleResponse> schedule = buildScheduleResponse(exam.getExamId(), tenantId);
            applyDateRange(schedule, examSummaryResponse::setStartDate, examSummaryResponse::setEndDate);
            examSummaryResponse.setCreatedBy(commonHelper.teacherName(exam.getCreatedBy()));
            examSummaryResponse.setUpdatedBy(commonHelper.teacherName(exam.getUpdatedBy()));
            examSummaryResponse.setUpdatedDate(exam.getUpdatedDate());
            examSummaryResponses.add(examSummaryResponse);
        }
        return examSummaryResponses;
    }

    @Override
    public ExamListResponse getExamById(Integer tenantId, Integer examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new CustomException("examId", "Exam not found"));

        if (!exam.getTenantId().equals(tenantId)) {
            throw new CustomException("examId", "Exam not found");
        }

        ClassMaster classMaster = classMasterRepository.findById(exam.getClassId()).orElse(null);

        ExamListResponse examListResponse = new ExamListResponse();
        examListResponse.setExamId(exam.getExamId());
        examListResponse.setClassId(exam.getClassId());
        examListResponse.setClassName(classMaster != null ? classMaster.getClassName() : null);
        examListResponse.setExamName(exam.getExamName());
        examListResponse.setMaxMarks(exam.getMaxMarks());
        examListResponse.setPassMarks(exam.getPassMarks());
        examListResponse.setExamDate(exam.getExamDate());
        List<ExamScheduleResponse> schedule = buildScheduleResponse(exam.getExamId(), tenantId);
        examListResponse.setSubjects(schedule);
        applyDateRange(schedule, examListResponse::setStartDate, examListResponse::setEndDate);
        examListResponse.setCreatedBy(commonHelper.teacherName(exam.getCreatedBy()));
        examListResponse.setUpdatedBy(commonHelper.teacherName(exam.getUpdatedBy()));
        examListResponse.setUpdatedDate(exam.getUpdatedDate());
        return examListResponse;
    }

    @Override
    @Transactional
    public boolean saveExam(Integer tenantId, Integer loginTeacherId, ExamRequest request) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);
        Exam exam;
        if (request.getExamId() != null) {
            exam = examRepository.findById(request.getExamId())
                    .orElseThrow(() -> new CustomException("examId", "Exam not found"));
        } else {
            exam = new Exam();
            exam.setTenantId(tenantId);
            exam.setAcademicYearId(currentYear.getAcademicYearId());
            exam.setIsActive(true);
            exam.setCreatedBy(loginTeacherId);
        }

        exam.setClassId(request.getClassId());
        exam.setExamName(request.getExamName());
        exam.setMaxMarks(request.getMaxMarks());
        exam.setPassMarks(request.getPassMarks() != null ? request.getPassMarks() : 0);
        exam.setExamDate(resolveExamStartDate(request));
        exam.setUpdatedBy(loginTeacherId);
        exam.setUpdatedDate(LocalDateTime.now());
        examRepository.save(exam);

        saveSchedule(tenantId, exam.getExamId(), request.getSubjects());
        return true;
    }

    private LocalDate resolveExamStartDate(ExamRequest request) {
        LocalDate earliest = null;
        if (request.getSubjects() != null) {
            for (ExamScheduleRequest item : request.getSubjects()) {
                if (item.getExamDate() != null && (earliest == null || item.getExamDate().isBefore(earliest))) {
                    earliest = item.getExamDate();
                }
            }
        }
        return earliest != null ? earliest : request.getExamDate();
    }

    private void saveSchedule(Integer tenantId, Integer examId, List<ExamScheduleRequest> subjects) {
        examScheduleRepository.deleteByExamIdAndTenantId(examId, tenantId);

        if (subjects == null || subjects.isEmpty()) {
            return;
        }

        for (ExamScheduleRequest examScheduleRequest : subjects) {
            if (examScheduleRequest.getSubjectId() == null || examScheduleRequest.getExamDate() == null) {
                continue;
            }
            ExamSchedule schedule = new ExamSchedule();
            schedule.setTenantId(tenantId);
            schedule.setExamId(examId);
            BeanUtils.copyProperties(examScheduleRequest, schedule);
            examScheduleRepository.save(schedule);
        }
    }

    private void applyDateRange(List<ExamScheduleResponse> schedule, Consumer<LocalDate> setStartDate, Consumer<LocalDate> setEndDate) {
        if (schedule == null || schedule.isEmpty()) {
            return;
        }
        setStartDate.accept(schedule.getFirst().getExamDate());
        setEndDate.accept(schedule.getLast().getExamDate());
    }

    private List<ExamScheduleResponse> buildScheduleResponse(Integer examId, Integer tenantId) {
        List<ExamSchedule> rows = examScheduleRepository
                .findByExamIdAndTenantIdOrderByExamDateAscExamScheduleIdAsc(examId, tenantId);

        List<ExamScheduleResponse> examScheduleResponses = new ArrayList<>();

        for (ExamSchedule examSchedule : rows) {
            Subject subject = subjectRepository.findById(examSchedule.getSubjectId()).orElse(null);

            ExamScheduleResponse response = new ExamScheduleResponse();
            BeanUtils.copyProperties(examSchedule, response);
            response.setSubjectName(subject != null ? subject.getSubjectName() : null);

            examScheduleResponses.add(response);
        }
        return examScheduleResponses;
    }

    @Override
    public boolean deleteExam(Integer tenantId, Integer examId) {
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new CustomException("examId", "Exam not found"));
        exam.setIsActive(false);
        examRepository.save(exam);
        return true;
    }

    @Override
    public ExamMarksEntryResponse getMarksEntry(Integer tenantId, Integer examId, Integer classId, Integer sectionId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new CustomException("examId", "Exam not found"));

        List<Subject> subjects = classSubjects(exam.getClassId(), tenantId);

        List<ExamMarksEntryResponse.SubjectItem> subjectItems = new ArrayList<>();
        for (Subject subject : subjects) {
            subjectItems.add(new ExamMarksEntryResponse.SubjectItem(subject.getSubjectId(), subject.getSubjectName()));
        }

        List<StudentClass> roster = studentClassRepository.findRoster(classId, sectionId, exam.getAcademicYearId(), tenantId);
        List<Integer> studentIds = new ArrayList<>();
        for (StudentClass studentClass : roster) {
            studentIds.add(studentClass.getStudentId());
        }

        // (studentId, subjectId) -> marks
        Map<Integer, Map<Integer, BigDecimal>> marksMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<ExamMark> marks = examMarkRepository.findByTenantIdAndExamIdAndStudentIdIn(tenantId, examId, studentIds);
            for (ExamMark examMark : marks) {
                marksMap.computeIfAbsent(examMark.getStudentId(), k -> new HashMap<>()).put(examMark.getSubjectId(), examMark.getMarksObtained());
            }
        }

        List<ExamMarksEntryResponse.StudentRow> rows = new ArrayList<>();
        for (StudentClass studentClass : roster) {
            ExamMarksEntryResponse.StudentRow row = new ExamMarksEntryResponse.StudentRow();
            row.setStudentId(studentClass.getStudentId());
            row.setStudentName(commonHelper.studentName(studentClass.getStudentId()));
            row.setRollNo(studentClass.getRollNo());
            row.setMarks(marksMap.getOrDefault(studentClass.getStudentId(), new HashMap<>()));
            rows.add(row);
        }
        rows.sort(Comparator.comparing(r -> CommonHelper.rollNo(r.getRollNo())));

        ExamMarksEntryResponse examMarksEntryResponse = new ExamMarksEntryResponse();
        examMarksEntryResponse.setExamId(exam.getExamId());
        examMarksEntryResponse.setExamName(exam.getExamName());
        examMarksEntryResponse.setMaxMarks(exam.getMaxMarks());
        examMarksEntryResponse.setSubjects(subjectItems);
        examMarksEntryResponse.setStudents(rows);
        return examMarksEntryResponse;
    }

    @Override
    @Transactional
    public boolean saveMarks(Integer tenantId, ExamMarksSaveRequest request) {
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new CustomException("examId", "Exam not found"));

        if (request.getRecords() == null || request.getRecords().isEmpty()) {
            throw new CustomException("records", "Please enter marks for at least one student.");
        }

        boolean hasMarks = false;
        for (ExamMarksSaveRequest.MarkItem item : request.getRecords()) {
            if (item.getMarksObtained() != null) {
                hasMarks = true;
                break;
            }
        }
        if (!hasMarks) {
            throw new CustomException("records", "Please enter marks for at least one student.");
        }

        BigDecimal maxMarks = BigDecimal.valueOf(exam.getMaxMarks());
        for (ExamMarksSaveRequest.MarkItem item : request.getRecords()) {
            if (item.getStudentId() == null || item.getSubjectId() == null) {
                continue;
            }
            if (item.getMarksObtained() != null
                    && (item.getMarksObtained().compareTo(BigDecimal.ZERO) < 0 || item.getMarksObtained().compareTo(maxMarks) > 0)) {
                throw new CustomException("marks", "Marks must be between 0 and " + exam.getMaxMarks());
            }

            ExamMark mark = examMarkRepository
                    .findByTenantIdAndExamIdAndStudentIdAndSubjectId(tenantId, exam.getExamId(), item.getStudentId(), item.getSubjectId())
                    .orElse(new ExamMark());

            mark.setTenantId(tenantId);
            mark.setExamId(exam.getExamId());
            mark.setStudentId(item.getStudentId());
            mark.setSubjectId(item.getSubjectId());
            mark.setAcademicYearId(exam.getAcademicYearId());
            mark.setMarksObtained(item.getMarksObtained());
            examMarkRepository.save(mark);
        }
        return true;
    }

    @Override
    public ReportCardResponse getReportCard(Integer tenantId, Integer examId, Integer studentId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new CustomException("examId", "Exam not found"));

        List<Subject> subjects = classSubjects(exam.getClassId(), tenantId);

        List<ExamMark> marks = examMarkRepository.findByTenantIdAndExamIdAndStudentId(tenantId, examId, studentId);
        Map<Integer, BigDecimal> markBySubject = new HashMap<>();
        for (ExamMark examMark : marks) {
            markBySubject.put(examMark.getSubjectId(), examMark.getMarksObtained());
        }

        BigDecimal passMarks = BigDecimal.valueOf(exam.getPassMarks());

        List<ReportCardResponse.SubjectMark> subjectMarks = new ArrayList<>();
        BigDecimal totalObtained = BigDecimal.ZERO;
        int totalMax = 0;
        boolean overallPass = true;

        for (Subject subject : subjects) {
            BigDecimal obtained = markBySubject.get(subject.getSubjectId());
            boolean passed = obtained != null && obtained.compareTo(passMarks) >= 0;
            if (!passed) {
                overallPass = false;
            }

            double subjectPct = obtained != null && exam.getMaxMarks() > 0
                    ? obtained.doubleValue() / exam.getMaxMarks() * 100.0
                    : 0.0;

            ReportCardResponse.SubjectMark subjectMark = new ReportCardResponse.SubjectMark();
            subjectMark.setSubjectId(subject.getSubjectId());
            subjectMark.setSubjectName(subject.getSubjectName());
            subjectMark.setMarksObtained(obtained);
            subjectMark.setGrade(obtained != null ? gradeFor(subjectPct) : "-");
            subjectMark.setPassed(passed);
            subjectMarks.add(subjectMark);

            totalObtained = totalObtained.add(obtained != null ? obtained : BigDecimal.ZERO);
            totalMax += exam.getMaxMarks();
        }

        double percentage = totalMax > 0
                ? totalObtained.multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(totalMax), 2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        ReportCardResponse reportCardResponse = new ReportCardResponse();
        reportCardResponse.setStudentId(studentId);
        reportCardResponse.setStudentName(commonHelper.studentName(studentId));
        StudentClass sc = studentClassRepository.findByStudentIdAndTenantId(studentId, tenantId).orElse(null);
        reportCardResponse.setRollNo(sc != null ? sc.getRollNo() : null);
        reportCardResponse.setExamName(exam.getExamName());
        reportCardResponse.setMaxMarksPerSubject(exam.getMaxMarks());
        reportCardResponse.setPassMarks(exam.getPassMarks());
        reportCardResponse.setSubjects(subjectMarks);
        reportCardResponse.setTotalObtained(totalObtained);
        reportCardResponse.setTotalMax(totalMax);
        reportCardResponse.setPercentage(percentage);
        reportCardResponse.setOverallGrade(gradeFor(percentage));
        reportCardResponse.setResult(overallPass ? "PASS" : "FAIL");
        return reportCardResponse;
    }
}

package com.edunest.service;

import com.edunest.dto.exam.ExamListResponse;
import com.edunest.dto.exam.ExamMarksEntryResponse;
import com.edunest.dto.exam.ExamMarksSaveRequest;
import com.edunest.dto.exam.ExamRequest;
import com.edunest.dto.exam.ExamScheduleRequest;
import com.edunest.dto.exam.ExamScheduleResponse;
import com.edunest.dto.exam.ReportCardResponse;
import com.edunest.entity.AcademicYear;
import com.edunest.entity.ClassMaster;
import com.edunest.entity.ClassSubject;
import com.edunest.entity.Exam;
import com.edunest.entity.ExamMark;
import com.edunest.entity.ExamSchedule;
import com.edunest.entity.Student;
import com.edunest.entity.StudentClass;
import com.edunest.entity.Subject;
import com.edunest.entity.Teacher;
import com.edunest.error.CustomException;
import com.edunest.repository.AcademicYearRepository;
import com.edunest.repository.ClassMasterRepository;
import com.edunest.repository.ClassSubjectRepository;
import com.edunest.repository.ExamMarkRepository;
import com.edunest.repository.ExamRepository;
import com.edunest.repository.ExamScheduleRepository;
import com.edunest.repository.StudentClassRepository;
import com.edunest.repository.StudentRepository;
import com.edunest.repository.SubjectRepository;
import com.edunest.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    ExamRepository examRepository;

    @Autowired
    ExamMarkRepository examMarkRepository;

    @Autowired
    StudentClassRepository studentClassRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    ClassSubjectRepository classSubjectRepository;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    ExamScheduleRepository examScheduleRepository;

    @Autowired
    ClassMasterRepository classMasterRepository;

    @Autowired
    TeacherRepository teacherRepository;

    @Autowired
    AcademicYearRepository academicYearRepository;

    private AcademicYear getCurrentYear(Integer tenantId) {
        AcademicYear currentYear = academicYearRepository.findByTenantIdAndIsCurrentTrue(tenantId);
        if (currentYear == null) {
            throw new CustomException("academicYear", "No active academic year found");
        }
        return currentYear;
    }

    private String studentName(Integer studentId) {
        Student student = studentRepository.findById(studentId).orElse(null);
        return student != null ? student.getFirstName() + " " + student.getLastName() : null;
    }

    private String teacherName(Integer teacherId) {
        if (teacherId == null) return null;
        Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
        return teacher != null ? teacher.getTeacherName() : null;
    }

    private List<Subject> classSubjects(Integer classId, Integer tenantId) {
        List<ClassSubject> mappings = classSubjectRepository.findByClassIdAndTenantId(classId, tenantId);
        List<Subject> subjects = new ArrayList<>();
        for (ClassSubject cs : mappings) {
            if (Boolean.FALSE.equals(cs.getIsActive())) continue;
            Subject subject = subjectRepository.findById(cs.getSubjectId()).orElse(null);
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
    public List<ExamListResponse> getExams(Integer tenantId, Integer classId) {
        AcademicYear currentYear = getCurrentYear(tenantId);

        List<Exam> exams = classId != null
                ? examRepository.findByTenantIdAndAcademicYearIdAndClassIdAndIsActiveTrueOrderByExamIdDesc(tenantId, currentYear.getAcademicYearId(), classId)
                : examRepository.findByTenantIdAndAcademicYearIdAndIsActiveTrueOrderByExamIdDesc(tenantId, currentYear.getAcademicYearId());

        List<ExamListResponse> result = new ArrayList<>();
        for (Exam exam : exams) {
            ClassMaster classMaster = classMasterRepository.findById(exam.getClassId()).orElse(null);

            ExamListResponse response = new ExamListResponse();
            response.setExamId(exam.getExamId());
            response.setClassId(exam.getClassId());
            response.setClassName(classMaster != null ? classMaster.getClassName() : null);
            response.setExamName(exam.getExamName());
            response.setMaxMarks(exam.getMaxMarks());
            response.setPassMarks(exam.getPassMarks());
            response.setExamDate(exam.getExamDate());
            response.setSubjects(buildScheduleResponse(exam.getExamId(), tenantId));
            response.setCreatedBy(teacherName(exam.getCreatedBy()));
            response.setUpdatedBy(teacherName(exam.getUpdatedBy()));
            response.setUpdatedDate(exam.getUpdatedDate());
            result.add(response);
        }
        return result;
    }

    @Override
    public ExamListResponse getExamById(Integer tenantId, Integer examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new CustomException("examId", "Exam not found"));

        if (!exam.getTenantId().equals(tenantId)) {
            throw new CustomException("examId", "Exam not found");
        }

        ClassMaster classMaster = classMasterRepository.findById(exam.getClassId()).orElse(null);

        ExamListResponse response = new ExamListResponse();
        response.setExamId(exam.getExamId());
        response.setClassId(exam.getClassId());
        response.setClassName(classMaster != null ? classMaster.getClassName() : null);
        response.setExamName(exam.getExamName());
        response.setMaxMarks(exam.getMaxMarks());
        response.setPassMarks(exam.getPassMarks());
        response.setExamDate(exam.getExamDate());
        response.setSubjects(buildScheduleResponse(exam.getExamId(), tenantId));
        response.setCreatedBy(teacherName(exam.getCreatedBy()));
        response.setUpdatedBy(teacherName(exam.getUpdatedBy()));
        response.setUpdatedDate(exam.getUpdatedDate());
        return response;
    }

    @Override
    @Transactional
    public boolean saveExam(Integer tenantId, Integer loginTeacherId, ExamRequest request) {
        AcademicYear currentYear = getCurrentYear(tenantId);
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

        for (ExamScheduleRequest item : subjects) {
            if (item.getSubjectId() == null || item.getExamDate() == null) {
                continue;
            }
            ExamSchedule schedule = new ExamSchedule();
            schedule.setTenantId(tenantId);
            schedule.setExamId(examId);
            schedule.setSubjectId(item.getSubjectId());
            schedule.setExamDate(item.getExamDate());
            schedule.setStartTime(item.getStartTime());
            schedule.setEndTime(item.getEndTime());
            schedule.setMaxMarks(item.getMaxMarks());
            schedule.setPassMarks(item.getPassMarks());
            examScheduleRepository.save(schedule);
        }
    }

    private List<ExamScheduleResponse> buildScheduleResponse(Integer examId, Integer tenantId) {
        List<ExamSchedule> rows = examScheduleRepository
                .findByExamIdAndTenantIdOrderByExamDateAscExamScheduleIdAsc(examId, tenantId);

        List<ExamScheduleResponse> result = new ArrayList<>();
        for (ExamSchedule row : rows) {
            Subject subject = subjectRepository.findById(row.getSubjectId()).orElse(null);

            ExamScheduleResponse response = new ExamScheduleResponse();
            response.setSubjectId(row.getSubjectId());
            response.setSubjectName(subject != null ? subject.getSubjectName() : null);
            response.setExamDate(row.getExamDate());
            response.setStartTime(row.getStartTime());
            response.setEndTime(row.getEndTime());
            response.setMaxMarks(row.getMaxMarks());
            response.setPassMarks(row.getPassMarks());
            result.add(response);
        }
        return result;
    }

    @Override
    public boolean deleteExam(Integer tenantId, Integer examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new CustomException("examId", "Exam not found"));
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
        for (Subject s : subjects) {
            subjectItems.add(new ExamMarksEntryResponse.SubjectItem(s.getSubjectId(), s.getSubjectName()));
        }

        List<StudentClass> roster = studentClassRepository.findRoster(classId, sectionId, exam.getAcademicYearId(), tenantId);
        List<Integer> studentIds = new ArrayList<>();
        for (StudentClass sc : roster) {
            studentIds.add(sc.getStudentId());
        }

        // (studentId, subjectId) -> marks
        Map<Integer, Map<Integer, BigDecimal>> marksMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<ExamMark> marks = examMarkRepository.findByTenantIdAndExamIdAndStudentIdIn(tenantId, examId, studentIds);
            for (ExamMark m : marks) {
                marksMap.computeIfAbsent(m.getStudentId(), k -> new HashMap<>()).put(m.getSubjectId(), m.getMarksObtained());
            }
        }

        List<ExamMarksEntryResponse.StudentRow> rows = new ArrayList<>();
        for (StudentClass sc : roster) {
            ExamMarksEntryResponse.StudentRow row = new ExamMarksEntryResponse.StudentRow();
            row.setStudentId(sc.getStudentId());
            row.setStudentName(studentName(sc.getStudentId()));
            row.setRollNo(sc.getRollNo());
            row.setMarks(marksMap.getOrDefault(sc.getStudentId(), new HashMap<>()));
            rows.add(row);
        }
        rows.sort(Comparator.comparing(r -> rollNoKey(r.getRollNo())));

        ExamMarksEntryResponse response = new ExamMarksEntryResponse();
        response.setExamId(exam.getExamId());
        response.setExamName(exam.getExamName());
        response.setMaxMarks(exam.getMaxMarks());
        response.setSubjects(subjectItems);
        response.setStudents(rows);
        return response;
    }

    @Override
    @Transactional
    public boolean saveMarks(Integer tenantId, ExamMarksSaveRequest request) {
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new CustomException("examId", "Exam not found"));

        if (request.getRecords() == null) {
            return true;
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
        for (ExamMark m : marks) {
            markBySubject.put(m.getSubjectId(), m.getMarksObtained());
        }

        BigDecimal maxMarks = BigDecimal.valueOf(exam.getMaxMarks());
        BigDecimal passMarks = BigDecimal.valueOf(exam.getPassMarks());

        List<ReportCardResponse.SubjectMark> subjectMarks = new ArrayList<>();
        BigDecimal totalObtained = BigDecimal.ZERO;
        int totalMax = 0;
        boolean overallPass = true;

        for (Subject s : subjects) {
            BigDecimal obtained = markBySubject.get(s.getSubjectId());
            boolean passed = obtained != null && obtained.compareTo(passMarks) >= 0;
            if (!passed) {
                overallPass = false;
            }

            double subjectPct = obtained != null && exam.getMaxMarks() > 0
                    ? obtained.doubleValue() / exam.getMaxMarks() * 100.0
                    : 0.0;

            ReportCardResponse.SubjectMark sm = new ReportCardResponse.SubjectMark();
            sm.setSubjectId(s.getSubjectId());
            sm.setSubjectName(s.getSubjectName());
            sm.setMarksObtained(obtained);
            sm.setGrade(obtained != null ? gradeFor(subjectPct) : "-");
            sm.setPassed(passed);
            subjectMarks.add(sm);

            totalObtained = totalObtained.add(obtained != null ? obtained : BigDecimal.ZERO);
            totalMax += exam.getMaxMarks();
        }

        double percentage = totalMax > 0
                ? totalObtained.multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(totalMax), 2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        ReportCardResponse response = new ReportCardResponse();
        response.setStudentId(studentId);
        response.setStudentName(studentName(studentId));
        StudentClass sc = studentClassRepository.findByStudentIdAndTenantId(studentId, tenantId).orElse(null);
        response.setRollNo(sc != null ? sc.getRollNo() : null);
        response.setExamName(exam.getExamName());
        response.setMaxMarksPerSubject(exam.getMaxMarks());
        response.setPassMarks(exam.getPassMarks());
        response.setSubjects(subjectMarks);
        response.setTotalObtained(totalObtained);
        response.setTotalMax(totalMax);
        response.setPercentage(percentage);
        response.setOverallGrade(gradeFor(percentage));
        response.setResult(overallPass ? "PASS" : "FAIL");
        return response;
    }

    private String rollNoKey(String rollNo) {
        if (rollNo == null || rollNo.isBlank()) {
            return "zzzzzzzzzz";
        }
        String trimmed = rollNo.trim();
        if (trimmed.matches("\\d+")) {
            return String.format("%010d", Long.parseLong(trimmed));
        }
        return trimmed;
    }
}

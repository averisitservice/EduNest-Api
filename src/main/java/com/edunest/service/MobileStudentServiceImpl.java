package com.edunest.service;

import com.edunest.common.PagedResponse;
import com.edunest.constant.Constant;
import com.edunest.dto.exam.ReportCardResponse;
import com.edunest.dto.mobile.*;
import com.edunest.entity.*;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class MobileStudentServiceImpl implements MobileStudentService {

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    StudentClassRepository studentClassRepository;

    @Autowired
    TeacherClassRepository teacherClassRepository;

    @Autowired
    TeacherRepository teacherRepository;

    @Autowired
    AttendanceRepository attendanceRepository;

    @Autowired
    LeaveRepository leaveRepository;

    @Autowired
    WorkingDayRepository workingDayRepository;

    @Autowired
    TimeSlotRepository timeSlotRepository;

    @Autowired
    TimetableRepository timetableRepository;

    @Autowired
    ExamRepository examRepository;

    @Autowired
    ExamScheduleRepository examScheduleRepository;

    @Autowired
    ExamMarkRepository examMarkRepository;

    @Autowired
    ExamService examService;

    @Autowired
    StudentNotificationService studentNotificationService;

    @Autowired
    AnnouncementRepository announcementRepository;

    @Autowired
    HomeworkRepository homeworkRepository;

    @Autowired
    NoteRepository noteRepository;

    @Autowired
    CommonHelper commonHelper;

    @Override
    public List<StudentHomeworkItem> getHomework(Integer studentId, Integer tenantId, LocalDate fromDate,
            LocalDate toDate) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);
        StudentClass studentClass = resolveStudentClass(studentId, tenantId);

        List<Homework> homeworkList = homeworkRepository.findHomeForStudentInDateRange(
                tenantId, currentYear.getAcademicYearId(),
                studentClass.getClassId(), studentClass.getSectionId(), fromDate, toDate);

        List<StudentHomeworkItem> studentHomeworkItems = new ArrayList<>();
        for (Homework homework : homeworkList) {
            StudentHomeworkItem item = new StudentHomeworkItem();
            item.setHomeworkId(homework.getHomeworkId());
            item.setSubjectName(commonHelper.subjectName(homework.getSubjectId()));
            item.setTitle(homework.getTitle());
            item.setDueDate(homework.getDueDate());
            item.setUpdatedDate(homework.getUpdatedDate());
            studentHomeworkItems.add(item);
        }
        return studentHomeworkItems;
    }

    @Override
    public List<StudentNoteItem> getNotes(Integer studentId, Integer tenantId, LocalDate fromDate, LocalDate toDate) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);
        StudentClass studentClass = resolveStudentClass(studentId, tenantId);

        List<Note> notes = noteRepository.findNoteForStudentInDateRange(
                tenantId, currentYear.getAcademicYearId(),
                studentClass.getClassId(), studentClass.getSectionId(),
                fromDate != null ? fromDate.atStartOfDay() : null,
                toDate != null ? toDate.atTime(23, 59, 59) : null);

        List<StudentNoteItem> studentNoteItems = new ArrayList<>();
        for (Note note : notes) {
            StudentNoteItem item = new StudentNoteItem();
            item.setNoteId(note.getNoteId());
            item.setSubjectName(commonHelper.subjectName(note.getSubjectId()));
            item.setTitle(note.getTitle());
            item.setUpdatedDate(note.getUpdatedDate());
            studentNoteItems.add(item);
        }
        return studentNoteItems;
    }

    @Override
    public StudentHomeworkDetailResponse getHomeworkDetail(Integer studentId, Integer tenantId, Integer homeworkId) {
        StudentClass studentClass = resolveStudentClass(studentId, tenantId);

        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new CustomException("homeworkId", "Homework not found"));

        if (!homework.getTenantId().equals(tenantId) || !homework.getClassId().equals(studentClass.getClassId())
                || (homework.getSectionId() != null && !homework.getSectionId().equals(studentClass.getSectionId()))) {
            throw new CustomException("homeworkId", "Homework not found");
        }

        StudentHomeworkDetailResponse studentHomeworkDetailResponse = new StudentHomeworkDetailResponse();
        studentHomeworkDetailResponse.setHomeworkId(homework.getHomeworkId());
        studentHomeworkDetailResponse.setSubjectName(commonHelper.subjectName(homework.getSubjectId()));
        studentHomeworkDetailResponse.setTitle(homework.getTitle());
        studentHomeworkDetailResponse.setDescription(homework.getDescription());
        studentHomeworkDetailResponse.setDueDate(homework.getDueDate());
        studentHomeworkDetailResponse.setAttachmentUrl(homework.getAttachmentUrl());
        studentHomeworkDetailResponse.setTeacherName(commonHelper.teacherNameForId(homework.getUpdatedBy()));
        studentHomeworkDetailResponse.setUpdatedDate(homework.getUpdatedDate());
        return studentHomeworkDetailResponse;
    }

    @Override
    public StudentNoteDetailResponse getNoteDetail(Integer studentId, Integer tenantId, Integer noteId) {
        StudentClass studentClass = resolveStudentClass(studentId, tenantId);

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new CustomException("noteId", "Note not found"));

        if (!note.getTenantId().equals(tenantId) || !note.getClassId().equals(studentClass.getClassId())
                || (note.getSectionId() != null && !note.getSectionId().equals(studentClass.getSectionId()))) {
            throw new CustomException("noteId", "Note not found");
        }

        StudentNoteDetailResponse response = new StudentNoteDetailResponse();
        response.setNoteId(note.getNoteId());
        response.setSubjectName(commonHelper.subjectName(note.getSubjectId()));
        response.setTitle(note.getTitle());
        response.setDescription(note.getDescription());
        response.setAttachmentUrl(note.getAttachmentUrl());
        response.setTeacherName(commonHelper.teacherNameForId(note.getUpdatedBy()));
        response.setUpdatedDate(note.getUpdatedDate());
        return response;
    }

    private StudentClass resolveStudentClass(Integer studentId, Integer tenantId) {
        return studentClassRepository
                .findByStudentIdAndTenantId(studentId, tenantId)
                .orElseThrow(() -> new CustomException("class", "You are not assigned to a class yet"));
    }

    @Override
    public StudentExamsResponse getExams(Integer studentId, Integer tenantId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        StudentClass studentClass = resolveStudentClass(studentId, tenantId);

        List<Exam> exams = examRepository
                .findByTenantIdAndAcademicYearIdAndClassIdAndIsActiveTrueOrderByExamIdDesc(
                        tenantId, currentYear.getAcademicYearId(), studentClass.getClassId());

        List<StudentExamsResponse.ExamItem> upcoming = new ArrayList<>();
        List<StudentExamsResponse.ExamItem> past = new ArrayList<>();

        for (Exam exam : exams) {
            List<ExamSchedule> examSchedules = examScheduleRepository
                    .findByExamIdAndTenantIdOrderByExamDateAscExamScheduleIdAsc(exam.getExamId(), tenantId);

            for (ExamSchedule examSchedule : examSchedules) {
                StudentExamsResponse.ExamItem examItem = new StudentExamsResponse.ExamItem();
                examItem.setExamName(exam.getExamName());
                examItem.setSubjectId(examSchedule.getSubjectId());
                examItem.setSubjectName(commonHelper.subjectName(examSchedule.getSubjectId()));
                examItem.setExamDate(examSchedule.getExamDate());
                examItem.setStartTime(examSchedule.getStartTime());
                examItem.setEndTime(examSchedule.getEndTime());
                examItem.setMaxMarks(
                        examSchedule.getMaxMarks() != null ? examSchedule.getMaxMarks() : exam.getMaxMarks());
                examItem.setPassMarks(
                        examSchedule.getPassMarks() != null ? examSchedule.getPassMarks() : exam.getPassMarks());

                if (examSchedule.getExamDate() != null && examSchedule.getExamDate().isBefore(LocalDate.now())) {
                    examItem.setStatus(Constant.EXAM_STATUS_COMPLETED);
                    past.add(examItem);
                } else {
                    examItem.setStatus(Constant.EXAM_STATUS_UPCOMING);
                    upcoming.add(examItem);
                }
            }
        }

        upcoming.sort(Comparator.comparing(StudentExamsResponse.ExamItem::getExamDate,
                Comparator.nullsLast(Comparator.naturalOrder())));
        past.sort(Comparator.comparing(StudentExamsResponse.ExamItem::getExamDate,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return new StudentExamsResponse(upcoming, past);
    }

    @Override
    public StudentTimetableResponse getTimetable(Integer studentId, Integer tenantId, String day) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        StudentClass studentClass = resolveStudentClass(studentId, tenantId);

        Integer classId = studentClass.getClassId();
        Integer sectionId = studentClass.getSectionId();

        StudentTimetableResponse studentTimetableResponse = new StudentTimetableResponse();
        studentTimetableResponse.setDisplayClass(commonHelper.displayClassForStudentClass(studentClass));

        List<WorkingDay> workingDays = workingDayRepository.findByTenantIdAndIsActiveTrueOrderByDayOrder(tenantId);
        List<TimeSlot> slots = timeSlotRepository
                .findByClassIdAndTenantIdAndIsActiveTrueOrderByOrderNo(classId, tenantId);
        List<Timetable> cells = timetableRepository
                .findCells(classId, sectionId, currentYear.getAcademicYearId(), tenantId);

        Map<String, Timetable> cellByKey = new HashMap<>();
        for (Timetable cell : cells) {
            cellByKey.put(cell.getWorkingDayId() + "-" + cell.getTimeSlotId(), cell);
        }

        String targetDay = resolveTargetDay(day, workingDays);

        List<StudentTimetableResponse.DaySchedule> days = new ArrayList<>();
        for (WorkingDay workingDay : workingDays) {
            List<StudentTimetableResponse.Period> periods = new ArrayList<>();

            if (workingDay.getDayName().equalsIgnoreCase(targetDay)) {
                for (TimeSlot slot : slots) {
                    StudentTimetableResponse.Period period = new StudentTimetableResponse.Period();
                    period.setSlotName(slot.getSlotName());
                    period.setStartTime(slot.getStartTime());
                    period.setEndTime(slot.getEndTime());
                    period.setIsBreak(slot.getIsBreak());

                    if (!Boolean.TRUE.equals(slot.getIsBreak())) {
                        Timetable cell = cellByKey.get(workingDay.getWorkingDayId() + "-" + slot.getTimeSlotId());
                        if (cell != null) {
                            period.setSubjectId(cell.getSubjectId());
                            period.setSubjectName(commonHelper.subjectName(cell.getSubjectId()));
                            period.setTeacherName(commonHelper.teacherNameForId(cell.getTeacherId()));
                        }
                    }

                    periods.add(period);
                }
            }

            days.add(new StudentTimetableResponse.DaySchedule(workingDay.getDayName(), periods));
        }

        studentTimetableResponse.setDays(days);
        return studentTimetableResponse;
    }

    private String resolveTargetDay(String requestedDay, List<WorkingDay> workingDays) {
        if (requestedDay != null && !requestedDay.isBlank()) {
            for (WorkingDay wd : workingDays) {
                if (wd.getDayName().equalsIgnoreCase(requestedDay.trim())) {
                    return wd.getDayName();
                }
            }
        }

        String todayName = LocalDate.now().getDayOfWeek().name();
        for (WorkingDay wd : workingDays) {
            if (wd.getDayName().equalsIgnoreCase(todayName)) {
                return wd.getDayName();
            }
        }

        return workingDays.isEmpty() ? null : workingDays.getFirst().getDayName();
    }

    @Override
    public StudentHomeResponse getStudentHome(Integer studentId, Integer tenantId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException("student", "Student not found"));

        if (!student.getTenantId().equals(tenantId)) {
            throw new CustomException("student", "Student not found");
        }

        Integer yearId = currentYear.getAcademicYearId();

        StudentHomeResponse response = new StudentHomeResponse();
        response.setStudentId(student.getStudentId());
        response.setStudentName(CommonHelper.studentNameForStudent(student));
        response.setPhotoUrl(student.getPhotoUrl());
        response.setAcademicYearName(currentYear.getYearName());

        StudentClass studentClass = studentClassRepository
                .findByStudentIdAndTenantId(studentId, tenantId)
                .orElse(null);
        if (studentClass != null) {
            response.setDisplayClass(commonHelper.displayClassForStudentClass(studentClass));
            response.setRollNo(studentClass.getRollNo());
        }

        LocalDate today = LocalDate.now();
        response.setTodayStatus(resolveTodayStatus(tenantId, studentId, yearId, today));

        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        long monthPresent = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDateBetweenAndStatus(
                        tenantId, studentId, yearId, monthStart, monthEnd, Constant.PRESENT);
        long monthAbsent = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDateBetweenAndStatus(
                        tenantId, studentId, yearId, monthStart, monthEnd, Constant.ABSENT);
        long monthLate = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDateBetweenAndStatus(
                        tenantId, studentId, yearId, monthStart, monthEnd, Constant.LEAVE);
        long monthTotal = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDateBetween(
                        tenantId, studentId, yearId, monthStart, monthEnd);

        response.setPresentDays(monthPresent);
        response.setAbsentDays(monthAbsent);
        response.setLateDays(monthLate);
        response.setThisMonthPercent(percent(monthPresent, monthTotal));

        long yearPresent = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearIdAndStatus(tenantId, studentId, yearId, Constant.PRESENT);
        long yearTotal = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearId(tenantId, studentId, yearId);

        response.setAveragePercent(percent(yearPresent, yearTotal));

        return response;
    }

    private String resolveTodayStatus(Integer tenantId, Integer studentId, Integer yearId, LocalDate today) {
        Attendance attendance = attendanceRepository
                .findByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDate(tenantId, studentId, yearId, today)
                .orElse(null);

        if (attendance == null) {
            return Constant.ATTENDANCE_DISPLAY_NOT_MARKED;
        }

        if (Constant.PRESENT.equals(attendance.getStatus())) {
            return Constant.ATTENDANCE_DISPLAY_PRESENT;
        } else if (Constant.ABSENT.equals(attendance.getStatus())) {
            return Constant.ATTENDANCE_DISPLAY_ABSENT;
        } else if (Constant.LEAVE.equals(attendance.getStatus())) {
            return Constant.ATTENDANCE_DISPLAY_LEAVE;
        } else {
            return Constant.ATTENDANCE_DISPLAY_NOT_MARKED;
        }
    }

    private double percent(long attended, long total) {
        if (total <= 0) {
            return 0.0;
        }
        return Math.round((attended * 10000.0 / total)) / 100.0;
    }

    @Override
    public StudentDetailResponse getStudentDetailsById(Integer studentId, Integer tenantId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException("studentId", "Student not found"));

        if (!student.getTenantId().equals(tenantId)) {
            throw new CustomException("studentId", "Student not found");
        }

        StudentDetailResponse studentDetailResponse = new StudentDetailResponse();
        studentDetailResponse.setStudentId(student.getStudentId());
        studentDetailResponse.setAdmissionNo(student.getAdmissionNo());
        studentDetailResponse.setStudentName(CommonHelper.studentNameForStudent(student));
        studentDetailResponse.setPhotoUrl(student.getPhotoUrl());

        studentDetailResponse.setDateOfBirth(student.getDateOfBirth());
        studentDetailResponse.setGender(student.getGender() != null ? String.valueOf(student.getGender()) : null);
        studentDetailResponse.setAadharNo(student.getAadharNo());
        studentDetailResponse.setEmail(student.getEmail());
        studentDetailResponse.setMobileNo(student.getMobileNo());

        studentDetailResponse.setFatherName(student.getFatherName());
        studentDetailResponse.setMotherName(student.getMotherName());
        studentDetailResponse.setParentMobile(student.getParentMobile());
        studentDetailResponse.setParentEmail(student.getParentEmail());
        studentDetailResponse.setParentAadhar(student.getParentAadhar());

        studentDetailResponse.setAddress(CommonHelper.fullAddressForStudent(student));

        applyClassPlacement(studentDetailResponse, student, tenantId);

        return studentDetailResponse;
    }

    private void applyClassPlacement(StudentDetailResponse response, Student student, Integer tenantId) {
        StudentClass studentClass = studentClassRepository
                .findByStudentIdAndTenantId(student.getStudentId(), tenantId)
                .orElse(null);

        if (studentClass == null) {
            return;
        }

        response.setDisplayClass(commonHelper.displayClassForStudentClass(studentClass));
        response.setRollNo(studentClass.getRollNo());
        response.setClassTeacherName(
                resolveClassTeacher(studentClass.getClassId(), studentClass.getSectionId(), tenantId));
    }

    private String resolveClassTeacher(Integer classId, Integer sectionId, Integer tenantId) {
        List<TeacherClass> assignments = teacherClassRepository
                .findByClassIdAndSectionIdAndTenantIdAndIsActiveTrue(classId, sectionId, tenantId);

        if (assignments.isEmpty()) {
            return null;
        }

        Teacher teacher = teacherRepository.findById(assignments.getFirst().getTeacherId()).orElse(null);
        if (teacher == null) {
            return null;
        }

        String name = CommonHelper.teacherNameForTeacher(teacher);
        return name.isEmpty() ? teacher.getTeacherName() : name;
    }

    @Override
    public StudentAttendanceResponse getAttendance(Integer studentId, Integer tenantId, LocalDate fromDate,
            LocalDate toDate) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        LocalDate resolvedToDate = toDate != null ? toDate : LocalDate.now();
        LocalDate resolvedFromDate = fromDate != null ? fromDate : resolvedToDate.minusDays(6);

        List<Attendance> attendanceList = attendanceRepository
                .findByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                        tenantId, studentId, currentYear.getAcademicYearId(), resolvedFromDate, resolvedToDate);

        Set<LocalDate> approvedLeaveDates = new HashSet<>();
        for (Leave leave : leaveRepository.findByTenantIdAndStudentIdAndLeaveDateBetweenAndStatus(
                tenantId, studentId, resolvedFromDate, resolvedToDate, Constant.LEAVE_STATUS_APPROVED)) {
            approvedLeaveDates.add(leave.getLeaveDate());
        }

        List<StudentAttendanceItem> records = new ArrayList<>();
        long presentDays = 0;
        long absentDays = 0;
        long lateDays = 0;

        for (Attendance attendance : attendanceList) {
            StudentAttendanceItem item = new StudentAttendanceItem();
            item.setAttendanceDate(attendance.getAttendanceDate());
            item.setDay(attendance.getAttendanceDate().getDayOfWeek().toString());

            boolean onApprovedLeave = approvedLeaveDates.contains(attendance.getAttendanceDate());

            if (Constant.PRESENT.equals(attendance.getStatus())) {
                item.setStatus(Constant.ATTENDANCE_DISPLAY_PRESENT);
                presentDays++;
            } else if (Constant.ABSENT.equals(attendance.getStatus())) {
                item.setStatus(
                        onApprovedLeave ? Constant.ATTENDANCE_DISPLAY_LEAVE : Constant.ATTENDANCE_DISPLAY_ABSENT);
                absentDays++;
            } else if (Constant.LEAVE.equals(attendance.getStatus())) {
                item.setStatus(Constant.ATTENDANCE_DISPLAY_LEAVE);
                lateDays++;
            } else {
                item.setStatus(Constant.ATTENDANCE_DISPLAY_NOT_MARKED);
            }

            records.add(item);
        }

        StudentAttendanceResponse response = new StudentAttendanceResponse();
        response.setFromDate(resolvedFromDate);
        response.setToDate(resolvedToDate);
        response.setPresentDays(presentDays);
        response.setAbsentDays(absentDays);
        response.setLateDays(lateDays);
        response.setTotalDays(attendanceList.size());
        response.setPercent(percent(presentDays, attendanceList.size()));
        response.setRecords(records);

        return response;
    }

    @Override
    public StudentResultsResponse getResults(Integer studentId, Integer tenantId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);
        StudentClass studentClass = resolveStudentClass(studentId, tenantId);

        List<Exam> exams = examRepository
                .findByTenantIdAndAcademicYearIdAndClassIdAndIsActiveTrueOrderByExamIdDesc(
                        tenantId, currentYear.getAcademicYearId(), studentClass.getClassId());

        List<StudentResultsResponse.ExamResultItem> examItems = new ArrayList<>();
        List<StudentResultsResponse.SubjectResult> subjectResults = new ArrayList<>();
        BigDecimal overallObtained = BigDecimal.ZERO;
        int overallMax = 0;

        for (Exam exam : exams) {
            List<ExamMark> marks = examMarkRepository
                    .findByTenantIdAndExamIdAndStudentId(tenantId, exam.getExamId(), studentId);
            if (marks.isEmpty()) {
                continue;
            }

            ReportCardResponse reportCard = examService.getReportCard(tenantId, exam.getExamId(), studentId);

            StudentResultsResponse.ExamResultItem examItem = new StudentResultsResponse.ExamResultItem();
            examItem.setExamId(exam.getExamId());
            examItem.setExamName(reportCard.getExamName());
            examItem.setExamDate(exam.getExamDate());
            examItem.setObtained(reportCard.getTotalObtained());
            examItem.setMax(reportCard.getTotalMax());
            examItem.setPercentage(reportCard.getPercentage());
            examItem.setGrade(reportCard.getOverallGrade());
            examItem.setResult(reportCard.getResult());
            examItems.add(examItem);

            overallObtained = overallObtained.add(reportCard.getTotalObtained());
            overallMax += reportCard.getTotalMax();

            for (ReportCardResponse.SubjectMark subjectMark : reportCard.getSubjects()) {
                StudentResultsResponse.SubjectResult subjectResult = findSubjectResult(subjectResults,
                        subjectMark.getSubjectId());
                if (subjectResult == null) {
                    subjectResult = new StudentResultsResponse.SubjectResult();
                    subjectResult.setSubjectId(subjectMark.getSubjectId());
                    subjectResult.setSubjectName(subjectMark.getSubjectName());
                    subjectResult.setObtained(BigDecimal.ZERO);
                    subjectResult.setMax(0);
                    subjectResults.add(subjectResult);
                }

                BigDecimal marksObtained = subjectMark.getMarksObtained() != null ? subjectMark.getMarksObtained()
                        : BigDecimal.ZERO;
                subjectResult.setObtained(subjectResult.getObtained().add(marksObtained));
                subjectResult.setMax(subjectResult.getMax() + reportCard.getMaxMarksPerSubject());
            }
        }

        for (StudentResultsResponse.SubjectResult subjectResult : subjectResults) {
            subjectResult.setPercentage(percentageOf(subjectResult.getObtained(), subjectResult.getMax()));
        }

        StudentResultsResponse response = new StudentResultsResponse();
        response.setOverallObtained(overallObtained);
        response.setOverallMax(overallMax);
        response.setOverallPercentage(percentageOf(overallObtained, overallMax));
        response.setSubjects(subjectResults);
        response.setExams(examItems);
        return response;
    }

    private StudentResultsResponse.SubjectResult findSubjectResult(
            List<StudentResultsResponse.SubjectResult> subjectResults, Integer subjectId) {
        for (StudentResultsResponse.SubjectResult subjectResult : subjectResults) {
            if (subjectResult.getSubjectId().equals(subjectId)) {
                return subjectResult;
            }
        }
        return null;
    }

    private double percentageOf(BigDecimal obtained, int max) {
        if (max <= 0) {
            return 0.0;
        }
        return Math.round(obtained.doubleValue() / max * 10000.0) / 100.0;
    }

    @Override
    public ReportCardResponse getResultDetail(Integer studentId, Integer tenantId, Integer examId) {
        StudentClass studentClass = resolveStudentClass(studentId, tenantId);

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new CustomException("examId", "Exam not found"));

        if (!exam.getTenantId().equals(tenantId) || !exam.getClassId().equals(studentClass.getClassId())) {
            throw new CustomException("examId", "Exam not found");
        }

        return examService.getReportCard(tenantId, examId, studentId);
    }

    @Override
    public PagedResponse<StudentNotificationItem> getNotifications(Integer studentId, Integer tenantId, int page,
            int size) {
        return studentNotificationService.getNotifications(tenantId, studentId, page, size);
    }

    @Override
    public boolean markNotificationAsRead(Integer studentId, Integer tenantId, Integer notificationId) {
        return studentNotificationService.markAsRead(tenantId, studentId, notificationId);
    }

    @Override
    public long getUnreadNotificationCount(Integer studentId, Integer tenantId) {
        return studentNotificationService.getUnreadCount(tenantId, studentId);
    }

    @Override
    public List<StudentAnnouncementItem> getAnnouncements(Integer studentId, Integer tenantId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);
        StudentClass studentClass = resolveStudentClass(studentId, tenantId);

        List<Announcement> announcements = announcementRepository
                .findByTenantIdAndAcademicYearIdAndIsActiveTrueOrderByPublishDateDescAnnouncementIdDesc(
                        tenantId, currentYear.getAcademicYearId());

        List<StudentAnnouncementItem> items = new ArrayList<>();
        for (Announcement announcement : announcements) {
            if (!Constant.ANNOUNCEMENT_STATUS_PUBLISHED.equalsIgnoreCase(announcement.getStatus())) {
                continue;
            }
            if (!isForClass(announcement, studentClass.getClassId())) {
                continue;
            }

            StudentAnnouncementItem item = new StudentAnnouncementItem();
            item.setAnnouncementId(announcement.getAnnouncementId());
            item.setTitle(announcement.getTitle());
            item.setMessage(announcement.getMessage());
            item.setPublishDate(announcement.getPublishDate());
            items.add(item);
        }
        return items;
    }

    private boolean isForClass(Announcement announcement, Integer classId) {
        if (Constant.All.equalsIgnoreCase(announcement.getAudience())
                || announcement.getClassIds() == null || announcement.getClassIds().isBlank()) {
            return true;
        }
        return commonHelper.convertClassIdsStringToList(announcement.getClassIds()).contains(classId);
    }
}

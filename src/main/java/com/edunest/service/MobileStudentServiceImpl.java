package com.edunest.service;

import com.edunest.dto.mobile.*;
import com.edunest.entity.*;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class MobileStudentServiceImpl implements MobileStudentService {

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    StudentClassRepository studentClassRepository;

    @Autowired
    ClassMasterRepository classMasterRepository;

    @Autowired
    ClassSectionRepository classSectionRepository;

    @Autowired
    TeacherClassRepository teacherClassRepository;

    @Autowired
    TeacherRepository teacherRepository;

    @Autowired
    AttendanceRepository attendanceRepository;

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
    HomeworkRepository homeworkRepository;

    @Autowired
    NoteRepository noteRepository;

    @Autowired
    CommonHelper commonHelper;

    @Override
    public List<StudentHomeworkItem> getHomework(Integer studentId, Integer tenantId, LocalDate fromDate, LocalDate toDate) {
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
        studentHomeworkDetailResponse.setTeacherName(commonHelper.teacherName(homework.getUpdatedBy()));
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
        response.setTeacherName(commonHelper.teacherName(note.getUpdatedBy()));
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

        StudentClass studentClass = studentClassRepository
                .findByStudentIdAndTenantId(studentId, tenantId)
                .orElseThrow(() -> new CustomException("class", "You are not assigned to a class yet"));

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
                examItem.setMaxMarks(examSchedule.getMaxMarks() != null ? examSchedule.getMaxMarks() : exam.getMaxMarks());
                examItem.setPassMarks(examSchedule.getPassMarks() != null ? examSchedule.getPassMarks() : exam.getPassMarks());

                if (examSchedule.getExamDate() != null && examSchedule.getExamDate().isBefore(LocalDate.now())) {
                    examItem.setStatus("Completed");
                    past.add(examItem);
                } else {
                    examItem.setStatus("Upcoming");
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

        StudentClass studentClass = studentClassRepository
                .findByStudentIdAndTenantId(studentId, tenantId)
                .orElseThrow(() -> new CustomException("class", "You are not assigned to a class yet"));

        Integer classId = studentClass.getClassId();
        Integer sectionId = studentClass.getSectionId();

        StudentTimetableResponse studentTimetableResponse = new StudentTimetableResponse();
        studentTimetableResponse.setDisplayClass(buildDisplayClass(studentClass));

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
                            period.setTeacherName(commonHelper.teacherName(cell.getTeacherId()));
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

    private String buildDisplayClass(StudentClass studentClass) {
        ClassMaster classMaster = classMasterRepository.findById(studentClass.getClassId()).orElse(null);
        String className = classMaster != null ? classMaster.getClassName() : null;
        String sectionName = null;
        if (studentClass.getSectionId() != null) {
            ClassSection classSection = classSectionRepository.findById(studentClass.getSectionId()).orElse(null);
            sectionName = classSection != null ? classSection.getSectionName() : null;
        }
        return (className != null && sectionName != null) ? className + " - " + sectionName : className;
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
        response.setStudentName(buildStudentName(student));
        response.setPhotoUrl(student.getPhotoUrl());
        response.setAcademicYearName(currentYear.getYearName());

        StudentClass studentClass = studentClassRepository
                .findByStudentIdAndTenantId(studentId, tenantId)
                .orElse(null);
        if (studentClass != null) {
            ClassMaster classMaster = classMasterRepository.findById(studentClass.getClassId()).orElse(null);
            String className = classMaster != null ? classMaster.getClassName() : null;
            String sectionName = null;
            if (studentClass.getSectionId() != null) {
                ClassSection classSection = classSectionRepository.findById(studentClass.getSectionId()).orElse(null);
                sectionName = classSection != null ? classSection.getSectionName() : null;
            }
            response.setDisplayClass(
                    (className != null && sectionName != null) ? className + " - " + sectionName : className);
            response.setRollNo(studentClass.getRollNo());
        }

        LocalDate today = LocalDate.now();
        response.setTodayStatus(resolveTodayStatus(tenantId, studentId, yearId, today));

        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        long monthPresent = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDateBetweenAndStatus(
                        tenantId, studentId, yearId, monthStart, monthEnd, "P");
        long monthAbsent = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDateBetweenAndStatus(
                        tenantId, studentId, yearId, monthStart, monthEnd, "A");
        long monthLate = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDateBetweenAndStatus(
                        tenantId, studentId, yearId, monthStart, monthEnd, "L");
        long monthTotal = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDateBetween(
                        tenantId, studentId, yearId, monthStart, monthEnd);

        response.setPresentDays(monthPresent);
        response.setAbsentDays(monthAbsent);
        response.setLateDays(monthLate);
        response.setThisMonthPercent(percent(monthPresent + monthLate, monthTotal));

        long yearPresent = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearIdAndStatus(tenantId, studentId, yearId, "P");
        long yearLate = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearIdAndStatus(tenantId, studentId, yearId, "L");
        long yearTotal = attendanceRepository
                .countByTenantIdAndStudentIdAndAcademicYearId(tenantId, studentId, yearId);

        response.setAveragePercent(percent(yearPresent + yearLate, yearTotal));

        return response;
    }

    private String resolveTodayStatus(Integer tenantId, Integer studentId, Integer yearId, LocalDate today) {
        return attendanceRepository
                .findByTenantIdAndStudentIdAndAcademicYearIdAndAttendanceDate(tenantId, studentId, yearId, today)
                .map(attendance -> switch (attendance.getStatus()) {
                    case "P" -> "PRESENT";
                    case "A" -> "ABSENT";
                    case "L" -> "LATE";
                    default -> "NOT_MARKED";
                })
                .orElse("NOT_MARKED");
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
        studentDetailResponse.setStudentName(buildStudentName(student));
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

        studentDetailResponse.setAddress(buildFullAddress(student));

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

        ClassMaster classMaster = classMasterRepository.findById(studentClass.getClassId()).orElse(null);
        String className = classMaster != null ? classMaster.getClassName() : null;

        String sectionName = null;
        if (studentClass.getSectionId() != null) {
            ClassSection classSection = classSectionRepository.findById(studentClass.getSectionId()).orElse(null);
            sectionName = classSection != null ? classSection.getSectionName() : null;
        }

        response.setDisplayClass(
                (className != null && sectionName != null) ? className + " - " + sectionName : className);
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

        Teacher teacher = teacherRepository.findById(assignments.get(0).getTeacherId()).orElse(null);
        if (teacher == null) {
            return null;
        }

        String name = ((teacher.getFirstName() != null ? teacher.getFirstName() : "") + " "
                + (teacher.getLastName() != null ? teacher.getLastName() : "")).trim();

        return name.isEmpty() ? teacher.getTeacherName() : name;
    }

    private String buildFullAddress(Student student) {
        List<String> parts = new ArrayList<>();

        if (hasText(student.getAddressLine1())) parts.add(student.getAddressLine1().trim());
        if (hasText(student.getCity())) parts.add(student.getCity().trim());
        if (hasText(student.getState())) parts.add(student.getState().trim());

        String address = String.join(", ", parts);

        if (hasText(student.getPostalCode())) {
            address = address.isEmpty()
                    ? student.getPostalCode().trim()
                    : address + " - " + student.getPostalCode().trim();
        }

        return address.isEmpty() ? null : address;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String buildStudentName(Student student) {
        StringBuilder name = new StringBuilder();
        if (student.getFirstName() != null) name.append(student.getFirstName());
        if (student.getLastName() != null) name.append(" ").append(student.getLastName());
        return name.toString().trim();
    }
}

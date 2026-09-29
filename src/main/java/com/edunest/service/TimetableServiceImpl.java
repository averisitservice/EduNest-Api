package com.edunest.service;

import com.edunest.dto.timeTable.TimeSlotRequest;
import com.edunest.dto.timeTable.TimetableRequest;
import com.edunest.dto.timeTable.TimetableResponse;
import com.edunest.dto.timeTable.WorkingDayRequest;
import com.edunest.entity.*;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TimetableServiceImpl implements TimetableService {

    @Autowired
    WorkingDayRepository workingDayRepository;

    @Autowired
    TimeSlotRepository timeSlotRepository;

    @Autowired
    TimetableRepository timetableRepository;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    TeacherRepository teacherRepository;

    @Autowired
    CommonHelper commonHelper;

    @Override
    @Transactional
    public boolean saveWorkingDays(Integer tenantId, WorkingDayRequest request) {
        for (WorkingDayRequest.WorkingDayItem workingDayItem : request.getWorkingDays()) {
            WorkingDay workingDay = workingDayRepository.findByTenantIdAndDayName(tenantId, workingDayItem.getDayName());
            workingDay.setTenantId(tenantId);
            workingDay.setDayName(workingDayItem.getDayName());
            workingDay.setDayOrder(workingDayItem.getDayOrder());
            workingDay.setIsActive(workingDayItem.getIsActive());
            workingDayRepository.save(workingDay);
        }
        return true;
    }

    @Override
    public List<WorkingDay> getWorkingDays(Integer tenantId) {
        return workingDayRepository.findByTenantIdAndIsActiveTrueOrderByDayOrder(tenantId);
    }

    @Override
    @Transactional
    public boolean saveTimeSlots(Integer tenantId, TimeSlotRequest request) {
        for (TimeSlotRequest.TimeSlotItem timeSlotItem : request.getTimeSlots()) {
            TimeSlot timeSlot;
            if (timeSlotItem.getTimeSlotId() != null) {
                timeSlot = timeSlotRepository.findById(timeSlotItem.getTimeSlotId()).orElse(new TimeSlot());
            } else {
                timeSlot = new TimeSlot();
            }
            timeSlot.setTenantId(tenantId);
            timeSlot.setClassId(request.getClassId());
            timeSlot.setSlotName(timeSlotItem.getSlotName());
            timeSlot.setStartTime(timeSlotItem.getStartTime());
            timeSlot.setEndTime(timeSlotItem.getEndTime());
            timeSlot.setIsBreak(timeSlotItem.getIsBreak() != null && timeSlotItem.getIsBreak());
            timeSlot.setOrderNo(timeSlotItem.getOrderNo());
            timeSlot.setIsActive(true);
            timeSlotRepository.save(timeSlot);
        }
        return true;
    }

    @Override
    public List<TimeSlot> getTimeSlots(Integer tenantId, Integer classId) {
        return timeSlotRepository.findByClassIdAndTenantIdAndIsActiveTrueOrderByOrderNo(classId, tenantId);
    }

    @Override
    public TimetableResponse getTimetable(Integer tenantId, Integer classId, Integer sectionId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        List<WorkingDay> workingDays = workingDayRepository.findByTenantIdAndIsActiveTrueOrderByDayOrder(tenantId);
        List<String> dayNames = new ArrayList<>();
        for (WorkingDay workingDay : workingDays) {
            dayNames.add(workingDay.getDayName());
        }

        List<TimeSlot> timeSlots = timeSlotRepository.findByClassIdAndTenantIdAndIsActiveTrueOrderByOrderNo(classId, tenantId);

        List<Timetable> timetables = timetableRepository.findCells(classId, sectionId, currentYear.getAcademicYearId(), tenantId);

        Map<String, TimetableResponse.CellData> timetableMap = new HashMap<>();
        for (Timetable timetable : timetables) {

            WorkingDay wd = workingDayRepository.findById(timetable.getWorkingDayId()).orElse(null);
            if (wd == null) continue;

            Subject subject = timetable.getSubjectId() != null ? subjectRepository.findById(timetable.getSubjectId()).orElse(null) : null;
            Teacher teacher = timetable.getTeacherId() != null ? teacherRepository.findById(timetable.getTeacherId()).orElse(null) : null;

            TimetableResponse.CellData cell = new TimetableResponse.CellData();
            cell.setTimetableId(timetable.getTimetableId());
            cell.setSubjectId(timetable.getSubjectId());
            cell.setSubjectName(subject != null ? subject.getSubjectName() : null);
            cell.setTeacherId(timetable.getTeacherId());
            cell.setTeacherName(CommonHelper.teacherNameForTeacher(teacher));

            timetableMap.put(timetable.getTimeSlotId() + "_" + wd.getDayName(), cell);
        }

        List<TimetableResponse.TimeSlotRow> rows = new ArrayList<>();
        for (TimeSlot timeSlot : timeSlots) {
            Map<String, TimetableResponse.CellData> cells = new LinkedHashMap<>();
            for (String dayName : dayNames) {
                String key = timeSlot.getTimeSlotId() + "_" + dayName;
                cells.put(dayName, timetableMap.getOrDefault(key, new TimetableResponse.CellData()));
            }
            TimetableResponse.TimeSlotRow row = new TimetableResponse.TimeSlotRow();
            row.setTimeSlotId(timeSlot.getTimeSlotId());
            row.setSlotName(timeSlot.getSlotName());
            row.setStartTime(timeSlot.getStartTime());
            row.setEndTime(timeSlot.getEndTime());
            row.setIsBreak(timeSlot.getIsBreak());
            row.setCells(cells);
            rows.add(row);
        }

        TimetableResponse timetableResponse = new TimetableResponse();
        timetableResponse.setWorkingDays(dayNames);
        timetableResponse.setRows(rows);
        return timetableResponse;
    }

    @Override
    @Transactional
    public boolean saveTimetableCell(Integer tenantId, TimetableRequest request) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        if (request.getTeacherId() != null) {
            TimeSlot requestedSlot = timeSlotRepository.findById(request.getTimeSlotId())
                    .orElseThrow(() -> new CustomException("timeSlot", "Time slot not found"));

            List<Timetable> teacherEntries = timetableRepository.findByTeacherIdAndWorkingDayIdAndAcademicYearIdAndTenantId(
                    request.getTeacherId(), request.getWorkingDayId(), currentYear.getAcademicYearId(), tenantId);

            for (Timetable timetable : teacherEntries) {
                boolean sameCell = timetable.getClassId().equals(request.getClassId())
                        && Objects.equals(timetable.getSectionId(), request.getSectionId())
                        && timetable.getTimeSlotId().equals(request.getTimeSlotId());
                if (sameCell) continue;

                TimeSlot existingSlot = timeSlotRepository.findById(timetable.getTimeSlotId()).orElse(null);
                if (existingSlot == null) continue;

                boolean overlaps = requestedSlot.getStartTime().isBefore(existingSlot.getEndTime())
                        && existingSlot.getStartTime().isBefore(requestedSlot.getEndTime());
                if (overlaps) {
                    throw new CustomException("teacherConflict", "Teacher is already assigned to another class at this time");
                }
            }
        }

        Timetable timetable = timetableRepository.findCell(request.getClassId(), request.getSectionId(), request.getWorkingDayId(), request.getTimeSlotId(), currentYear.getAcademicYearId(), tenantId).orElse(new Timetable());

        timetable.setTenantId(tenantId);
        timetable.setClassId(request.getClassId());
        timetable.setSectionId(request.getSectionId());
        timetable.setWorkingDayId(request.getWorkingDayId());
        timetable.setTimeSlotId(request.getTimeSlotId());
        timetable.setSubjectId(request.getSubjectId());
        timetable.setTeacherId(request.getTeacherId());
        timetable.setAcademicYearId(currentYear.getAcademicYearId());
        timetable.setIsActive(true);
        timetableRepository.save(timetable);
        return true;
    }

    @Override
    public TimetableResponse getTeacherTimetable(Integer tenantId, Integer teacherId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        List<WorkingDay> workingDays = workingDayRepository.findByTenantIdAndIsActiveTrueOrderByDayOrder(tenantId);
        List<String> dayNames = new ArrayList<>();
        for (WorkingDay workingDay : workingDays) {
            dayNames.add(workingDay.getDayName());
        }

        List<Timetable> timetables = timetableRepository.findByTeacherIdAndAcademicYearIdAndTenantId(teacherId, currentYear.getAcademicYearId(), tenantId);

        Map<String, TimetableResponse.CellData> timetableMap = new HashMap<>();
        Set<Integer> timeSlotIds = new LinkedHashSet<>();

        for (Timetable timetable : timetables) {
            timeSlotIds.add(timetable.getTimeSlotId());
            WorkingDay wd = workingDayRepository.findById(timetable.getWorkingDayId()).orElse(null);
            Subject subject = timetable.getSubjectId() != null ? subjectRepository.findById(timetable.getSubjectId()).orElse(null) : null;

            TimetableResponse.CellData cell = new TimetableResponse.CellData();
            cell.setTimetableId(timetable.getTimetableId());
            cell.setSubjectId(timetable.getSubjectId());
            cell.setSubjectName(subject != null ? subject.getSubjectName() : null);

            if (wd != null) {
                timetableMap.put(timetable.getTimeSlotId() + "_" + wd.getDayName(), cell);
            }
        }

        List<TimetableResponse.TimeSlotRow> rows = new ArrayList<>();
        for (Integer timeSlot : timeSlotIds) {
            TimeSlot ts = timeSlotRepository.findById(timeSlot).orElse(null);
            if (ts == null) continue;

            Map<String, TimetableResponse.CellData> cells = new LinkedHashMap<>();
            for (String dayName : dayNames) {
                String key = timeSlot + "_" + dayName;
                cells.put(dayName, timetableMap.getOrDefault(key, new TimetableResponse.CellData()));
            }

            TimetableResponse.TimeSlotRow row = new TimetableResponse.TimeSlotRow();
            row.setTimeSlotId(ts.getTimeSlotId());
            row.setSlotName(ts.getSlotName());
            row.setStartTime(ts.getStartTime());
            row.setEndTime(ts.getEndTime());
            row.setIsBreak(ts.getIsBreak());
            row.setCells(cells);
            rows.add(row);
        }

        TimetableResponse response = new TimetableResponse();
        response.setWorkingDays(dayNames);
        response.setRows(rows);
        return response;
    }
}
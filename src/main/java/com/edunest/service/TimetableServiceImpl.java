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
        for (WorkingDayRequest.WorkingDayItem item : request.getWorkingDays()) {
            WorkingDay workingDay = workingDayRepository.findByTenantIdAndDayName(tenantId, item.getDayName());
            workingDay.setTenantId(tenantId);
            workingDay.setDayName(item.getDayName());
            workingDay.setDayOrder(item.getDayOrder());
            workingDay.setIsActive(item.getIsActive());
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
        for (TimeSlotRequest.TimeSlotItem item : request.getTimeSlots()) {
            TimeSlot timeSlot;
            if (item.getTimeSlotId() != null) {
                timeSlot = timeSlotRepository.findById(item.getTimeSlotId()).orElse(new TimeSlot());
            } else {
                timeSlot = new TimeSlot();
            }
            timeSlot.setTenantId(tenantId);
            timeSlot.setClassId(request.getClassId());
            timeSlot.setSlotName(item.getSlotName());
            timeSlot.setStartTime(item.getStartTime());
            timeSlot.setEndTime(item.getEndTime());
            timeSlot.setIsBreak(item.getIsBreak() != null && item.getIsBreak());
            timeSlot.setOrderNo(item.getOrderNo());
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
        for (WorkingDay wd : workingDays) {
            dayNames.add(wd.getDayName());
        }

        List<TimeSlot> timeSlots = timeSlotRepository.findByClassIdAndTenantIdAndIsActiveTrueOrderByOrderNo(classId, tenantId);

        List<Timetable> timetables = timetableRepository.findCells(classId, sectionId, currentYear.getAcademicYearId(), tenantId);

        Map<String, TimetableResponse.CellData> timetableMap = new HashMap<>();
        for (Timetable tt : timetables) {

            WorkingDay wd = workingDayRepository.findById(tt.getWorkingDayId()).orElse(null);
            if (wd == null) continue;

            Subject subject = tt.getSubjectId() != null ? subjectRepository.findById(tt.getSubjectId()).orElse(null) : null;
            Teacher teacher = tt.getTeacherId() != null ? teacherRepository.findById(tt.getTeacherId()).orElse(null) : null;

            TimetableResponse.CellData cell = new TimetableResponse.CellData();
            cell.setTimetableId(tt.getTimetableId());
            cell.setSubjectId(tt.getSubjectId());
            cell.setSubjectName(subject != null ? subject.getSubjectName() : null);
            cell.setTeacherId(tt.getTeacherId());
            cell.setTeacherName(teacher != null ? teacher.getFirstName() + " " + teacher.getLastName() : null);

            timetableMap.put(tt.getTimeSlotId() + "_" + wd.getDayName(), cell);
        }

        List<TimetableResponse.TimeSlotRow> rows = new ArrayList<>();
        for (TimeSlot ts : timeSlots) {
            Map<String, TimetableResponse.CellData> cells = new LinkedHashMap<>();
            for (String dayName : dayNames) {
                String key = ts.getTimeSlotId() + "_" + dayName;
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

    @Override
    @Transactional
    public boolean saveTimetableCell(Integer tenantId, TimetableRequest request) {

        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        if (request.getTeacherId() != null) {
            TimeSlot requestedSlot = timeSlotRepository.findById(request.getTimeSlotId())
                    .orElseThrow(() -> new CustomException("timeSlot", "Time slot not found"));

            List<Timetable> teacherEntries = timetableRepository.findByTeacherIdAndWorkingDayIdAndAcademicYearIdAndTenantId(
                    request.getTeacherId(), request.getWorkingDayId(), currentYear.getAcademicYearId(), tenantId);

            for (Timetable entry : teacherEntries) {
                boolean sameCell = entry.getClassId().equals(request.getClassId())
                        && Objects.equals(entry.getSectionId(), request.getSectionId())
                        && entry.getTimeSlotId().equals(request.getTimeSlotId());
                if (sameCell) continue;

                TimeSlot existingSlot = timeSlotRepository.findById(entry.getTimeSlotId()).orElse(null);
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
        for (WorkingDay wd : workingDays) {
            dayNames.add(wd.getDayName());
        }

        List<Timetable> timetables = timetableRepository.findByTeacherIdAndAcademicYearIdAndTenantId(teacherId, currentYear.getAcademicYearId(), tenantId);

        Map<String, TimetableResponse.CellData> timetableMap = new HashMap<>();
        Set<Integer> timeSlotIds = new LinkedHashSet<>();

        for (Timetable tt : timetables) {
            timeSlotIds.add(tt.getTimeSlotId());
            WorkingDay wd = workingDayRepository.findById(tt.getWorkingDayId()).orElse(null);
            Subject subject = tt.getSubjectId() != null ? subjectRepository.findById(tt.getSubjectId()).orElse(null) : null;

            TimetableResponse.CellData cell = new TimetableResponse.CellData();
            cell.setTimetableId(tt.getTimetableId());
            cell.setSubjectId(tt.getSubjectId());
            cell.setSubjectName(subject != null ? subject.getSubjectName() : null);

            if (wd != null) {
                timetableMap.put(tt.getTimeSlotId() + "_" + wd.getDayName(), cell);
            }
        }

        List<TimetableResponse.TimeSlotRow> rows = new ArrayList<>();
        for (Integer tsId : timeSlotIds) {
            TimeSlot ts = timeSlotRepository.findById(tsId).orElse(null);
            if (ts == null) continue;

            Map<String, TimetableResponse.CellData> cells = new LinkedHashMap<>();
            for (String dayName : dayNames) {
                String key = tsId + "_" + dayName;
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
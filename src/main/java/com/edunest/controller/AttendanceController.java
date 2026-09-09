package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.attendance.AttendanceRosterResponse;
import com.edunest.dto.attendance.AttendanceSaveRequest;
import com.edunest.dto.attendance.AttendanceSummaryResponse;
import com.edunest.service.AttendanceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    AttendanceService attendanceService;

    @Autowired
    JwtHelper jwtHelper;

    @GetMapping("/roster/{classId}")
    public ResponseEntity<ResponseObject<AttendanceRosterResponse>> getRoster(
            HttpServletRequest request,
            @PathVariable Integer classId,
            @RequestParam(required = false) Integer sectionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String search) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<AttendanceRosterResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(attendanceService.getRoster(tenantId, classId, sectionId, date, search));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ResponseObject<Boolean>> saveAttendance(
            HttpServletRequest request, @RequestBody AttendanceSaveRequest attendanceRequest) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);
        Integer markedBy = jwtHelper.extractTeacherId(token);

        ResponseObject<Boolean> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(attendanceService.saveAttendance(tenantId, markedBy, attendanceRequest));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary/{classId}")
    public ResponseEntity<ResponseObject<List<AttendanceSummaryResponse>>> getSummary(
            HttpServletRequest request,
            @PathVariable Integer classId,
            @RequestParam(required = false) Integer sectionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<List<AttendanceSummaryResponse>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(attendanceService.getSummary(tenantId, classId, sectionId, fromDate, toDate));
        return ResponseEntity.ok(response);
    }
}

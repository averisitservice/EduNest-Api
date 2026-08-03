package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.mobile.StudentAttendanceResponse;
import com.edunest.dto.mobile.StudentDetailResponse;
import com.edunest.dto.mobile.StudentExamsResponse;
import com.edunest.dto.mobile.StudentHomeResponse;
import com.edunest.dto.mobile.StudentHomeworkDetailResponse;
import com.edunest.dto.mobile.StudentHomeworkItem;
import com.edunest.dto.mobile.StudentNoteDetailResponse;
import com.edunest.dto.mobile.StudentNoteItem;
import com.edunest.dto.mobile.StudentTimetableResponse;
import com.edunest.service.MobileStudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/student")
public class MobileStudentController {

    @Autowired
    MobileStudentService mobileStudentService;

    @Autowired
    JwtHelper jwtHelper;

    @GetMapping("/home")
    public ResponseEntity<ResponseObject<StudentHomeResponse>> getStudentHome(HttpServletRequest request) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<StudentHomeResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(mobileStudentService.getStudentHome(studentId, tenantId));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/timetable")
    public ResponseEntity<ResponseObject<StudentTimetableResponse>> getTimetable(
            HttpServletRequest request,
            @RequestParam(required = false) String day) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<StudentTimetableResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(mobileStudentService.getTimetable(studentId, tenantId, day));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/exams")
    public ResponseEntity<ResponseObject<StudentExamsResponse>> getExams(HttpServletRequest request) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<StudentExamsResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(mobileStudentService.getExams(studentId, tenantId));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/homework")
    public ResponseEntity<ResponseObject<List<StudentHomeworkItem>>> getHomework(
            HttpServletRequest request,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<List<StudentHomeworkItem>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(mobileStudentService.getHomework(studentId, tenantId, fromDate, toDate));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/notes")
    public ResponseEntity<ResponseObject<List<StudentNoteItem>>> getNotes(
            HttpServletRequest request,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<List<StudentNoteItem>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(mobileStudentService.getNotes(studentId, tenantId, fromDate, toDate));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/attendance")
    public ResponseEntity<ResponseObject<StudentAttendanceResponse>> getAttendance(
            HttpServletRequest request,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<StudentAttendanceResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(mobileStudentService.getAttendance(studentId, tenantId, fromDate, toDate));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/homework/{homeworkId}")
    public ResponseEntity<ResponseObject<StudentHomeworkDetailResponse>> getHomeworkDetail(
            HttpServletRequest request, @PathVariable Integer homeworkId) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<StudentHomeworkDetailResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(mobileStudentService.getHomeworkDetail(studentId, tenantId, homeworkId));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/notes/{noteId}")
    public ResponseEntity<ResponseObject<StudentNoteDetailResponse>> getNoteDetail(
            HttpServletRequest request, @PathVariable Integer noteId) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<StudentNoteDetailResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(mobileStudentService.getNoteDetail(studentId, tenantId, noteId));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<ResponseObject<StudentDetailResponse>> getStudentDetailsById(
            HttpServletRequest request, @PathVariable Integer studentId) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<StudentDetailResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(mobileStudentService.getStudentDetailsById(studentId, tenantId));

        return ResponseEntity.ok(response);
    }
}

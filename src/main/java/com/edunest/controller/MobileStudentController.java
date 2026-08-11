package com.edunest.controller;

import com.edunest.common.PagedResponse;
import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.exam.ReportCardResponse;
import com.edunest.dto.mobile.StudentAttendanceResponse;
import com.edunest.dto.mobile.StudentDetailResponse;
import com.edunest.dto.mobile.StudentExamsResponse;
import com.edunest.dto.mobile.StudentHomeResponse;
import com.edunest.dto.mobile.StudentHomeworkDetailResponse;
import com.edunest.dto.mobile.StudentHomeworkItem;
import com.edunest.dto.mobile.StudentNoteDetailResponse;
import com.edunest.dto.mobile.StudentNoteItem;
import com.edunest.dto.mobile.StudentNotificationItem;
import com.edunest.dto.mobile.StudentResultsResponse;
import com.edunest.dto.mobile.StudentTimetableResponse;
import com.edunest.service.MobileStudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @GetMapping("/results")
    public ResponseEntity<ResponseObject<StudentResultsResponse>> getResults(HttpServletRequest request) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<StudentResultsResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(mobileStudentService.getResults(studentId, tenantId));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/results/{examId}")
    public ResponseEntity<ResponseObject<ReportCardResponse>> getResultDetail(
            HttpServletRequest request, @PathVariable Integer examId) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<ReportCardResponse> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(mobileStudentService.getResultDetail(studentId, tenantId, examId));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/notifications")
    public ResponseEntity<ResponseObject<PagedResponse<StudentNotificationItem>>> getNotifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<PagedResponse<StudentNotificationItem>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(mobileStudentService.getNotifications(studentId, tenantId, page, size));

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<ResponseObject<Boolean>> markNotificationAsRead(
            HttpServletRequest request, @PathVariable Integer notificationId) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<Boolean> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(mobileStudentService.markNotificationAsRead(studentId, tenantId, notificationId));

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

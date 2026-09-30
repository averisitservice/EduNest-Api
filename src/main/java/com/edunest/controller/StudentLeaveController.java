package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.leave.LeaveRequest;
import com.edunest.dto.leave.LeaveResponse;
import com.edunest.service.LeaveService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/leave")
public class StudentLeaveController {

    @Autowired
    LeaveService leaveService;

    @Autowired
    JwtHelper jwtHelper;

    @GetMapping("/list")
    public ResponseEntity<ResponseObject<List<LeaveResponse>>> getStudentLeaveList(HttpServletRequest request) {
        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<List<LeaveResponse>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(leaveService.getStudentLeaveList(tenantId, studentId));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ResponseObject<Boolean>> submitStudentLeave(
            HttpServletRequest request, @RequestBody LeaveRequest leaveRequest) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<Boolean> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(leaveService.submitStudentLeave(tenantId, studentId, leaveRequest));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{leaveId}")
    public ResponseEntity<ResponseObject<Boolean>> deleteStudentLeave(
            HttpServletRequest request, @PathVariable Integer leaveId) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<Boolean> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(leaveService.deleteStudentLeave(tenantId, studentId, leaveId));
        return ResponseEntity.ok(response);
    }
}

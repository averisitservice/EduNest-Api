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
public class LeaveController {

    @Autowired
    LeaveService leaveService;

    @Autowired
    JwtHelper jwtHelper;

    @GetMapping("/list")
    public ResponseEntity<ResponseObject<List<LeaveResponse>>> getLeaveList(HttpServletRequest request) {
        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<List<LeaveResponse>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(leaveService.getLeaveList(tenantId, studentId));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ResponseObject<Boolean>> submitLeave(
            HttpServletRequest request, @RequestBody LeaveRequest leaveRequest) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<Boolean> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(leaveService.submitLeave(tenantId, studentId, leaveRequest));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{leaveId}")
    public ResponseEntity<ResponseObject<Boolean>> deleteLeave(
            HttpServletRequest request, @PathVariable Integer leaveId) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer studentId = jwtHelper.extractStudentId(token);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<Boolean> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(leaveService.deleteLeave(tenantId, studentId, leaveId));
        return ResponseEntity.ok(response);
    }
}

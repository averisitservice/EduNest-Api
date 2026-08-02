package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.leave.LeaveListResponse;
import com.edunest.dto.leave.LeaveStatusRequest;
import com.edunest.service.LeaveService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leave")
public class LeaveTeacherController {

    @Autowired
    LeaveService leaveService;

    @Autowired
    JwtHelper jwtHelper;

    @GetMapping("/list/{classId}")
    public ResponseEntity<ResponseObject<List<LeaveListResponse>>> getLeaveList(
            HttpServletRequest request,
            @PathVariable Integer classId,
            @RequestParam(required = false) Integer sectionId) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<List<LeaveListResponse>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(leaveService.getLeaveListForClass(tenantId, classId, sectionId));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{leaveId}/status")
    public ResponseEntity<ResponseObject<Boolean>> updateStatus(
            HttpServletRequest request,
            @PathVariable Integer leaveId,
            @RequestBody LeaveStatusRequest leaveStatusRequest) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer tenantId = jwtHelper.extractTenantId(token);
        Integer teacherId = jwtHelper.extractTeacherId(token);

        ResponseObject<Boolean> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(leaveService.updateLeaveStatus(tenantId, teacherId, leaveId, leaveStatusRequest.getStatus()));
        return ResponseEntity.ok(response);
    }
}

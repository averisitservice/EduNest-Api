package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.teacher.TeacherDTO;
import com.edunest.dto.teacher.TeacherListResponse;
import com.edunest.service.TeacherService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    TeacherService teacherService;

    @Autowired
    JwtHelper jwtHelper;

    @GetMapping("/list")
    public ResponseEntity<ResponseObject<List<TeacherListResponse>>> getTeacherList(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);
        int teacherId = jwtHelper.extractTeacherId(token);

        ResponseObject<List<TeacherListResponse>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(teacherService.getTeacherList(tenantId, teacherId));

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ResponseObject<Boolean>> saveTeacher(HttpServletRequest request, @RequestBody TeacherDTO teacherDTO) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);
        Integer loginTeacherId = jwtHelper.extractTeacherId(token);

        ResponseObject<Boolean> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(teacherService.saveTeacher(teacherDTO.getTeacherId(), tenantId, loginTeacherId, teacherDTO));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<ResponseObject<List<TeacherListResponse>>> getTeachersBySubject(HttpServletRequest request, @PathVariable Integer subjectId) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<List<TeacherListResponse>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(teacherService.getTeachersBySubject(tenantId, subjectId));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{teacherId}")
    public ResponseEntity<ResponseObject<TeacherDTO>> getTeacherById(@PathVariable Integer teacherId) {

        ResponseObject<TeacherDTO> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(teacherService.getTeacherById(teacherId));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{teacherId}")
    public ResponseEntity<ResponseObject<String>> deleteTeacher(HttpServletRequest request, @PathVariable Integer teacherId) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        int loginTeacherId = jwtHelper.extractTeacherId(token);

        ResponseObject<String> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(teacherService.deleteTeacher(teacherId, loginTeacherId) ? "Teacher deleted successfully" : "Teacher not deleted");

        return ResponseEntity.ok(response);
    }

}

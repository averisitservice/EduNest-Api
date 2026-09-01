package com.edunest.controller;

import com.edunest.common.PagedResponse;
import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.student.StudentListResponse;
import com.edunest.dto.student.StudentDTO;
import com.edunest.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    StudentService studentService;

    @Autowired
    JwtHelper jwtHelper;

    @GetMapping("/list")
    public ResponseEntity<ResponseObject<PagedResponse<StudentListResponse>>> getStudentList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) Integer sectionId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<PagedResponse<StudentListResponse>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(studentService.getStudentList(tenantId, page, size, search, classId, sectionId, sortBy, sortDir));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<ResponseObject<StudentDTO>> getStudentById(HttpServletRequest request, @PathVariable Integer studentId) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<StudentDTO> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(studentService.getStudentById(studentId, tenantId));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ResponseObject<Boolean>> saveStudent(HttpServletRequest request, @RequestBody StudentDTO studentDTO) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);
        Integer loginTeacherId = jwtHelper.extractTeacherId(token);

        ResponseObject<Boolean> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(studentService.saveStudent(tenantId, loginTeacherId, studentDTO));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<ResponseObject<String>> deleteStudent(HttpServletRequest request, @PathVariable Integer studentId) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);
        Integer loginTeacherId = jwtHelper.extractTeacherId(token);

        boolean isDeleted = studentService.deleteStudent(tenantId, studentId, loginTeacherId);

        ResponseObject<String> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(isDeleted ? "Student deleted successfully" : "Student not deleted");
        return ResponseEntity.ok(response);
    }
}
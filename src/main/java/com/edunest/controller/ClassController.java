package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.classes.ClassListResponse;
import com.edunest.dto.classes.ClassDTO;
import com.edunest.entity.Subject;
import com.edunest.service.ClassService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/class")
public class ClassController {

    @Autowired
    ClassService classService;

    @Autowired
    JwtHelper jwtHelper;

    @GetMapping("/list")
    public ResponseEntity<ResponseObject<List<ClassListResponse>>> getClassList(HttpServletRequest request) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<List<ClassListResponse>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(classService.getClassList(tenantId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{classId}")
    public ResponseEntity<ResponseObject<ClassDTO>> getClassById(HttpServletRequest request, @PathVariable Integer classId) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<ClassDTO> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(classService.getClassById(classId, tenantId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{classId}/subjects")
    public ResponseEntity<ResponseObject<List<Subject>>> getClassSubjects(HttpServletRequest request, @PathVariable Integer classId) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<List<Subject>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(classService.getClassSubjects(classId, tenantId));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ResponseObject<Boolean>> saveClass(HttpServletRequest request, @RequestBody ClassDTO classDTO) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<Boolean> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(classService.saveClass(classDTO.getClassId(), tenantId, classDTO));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{classId}")
    public ResponseEntity<ResponseObject<String>> deleteClass(@PathVariable Integer classId) {

        boolean isDeleted = classService.deleteClass(classId);

        ResponseObject<String> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(isDeleted ? "Class deleted successfully" : "Class not deleted");
        return ResponseEntity.ok(response);
    }
}
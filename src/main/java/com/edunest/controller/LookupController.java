package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.classes.ClassSectionResponse;
import com.edunest.entity.ClassMaster;
import com.edunest.entity.EmploymentType;
import com.edunest.entity.Role;
import com.edunest.entity.Subject;
import com.edunest.service.LookupService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lookup")
public class LookupController {

    @Autowired
    LookupService lookupService;

    @Autowired
    JwtHelper jwtHelper;

    @GetMapping("/roles")
    public ResponseEntity<ResponseObject<List<Role>>> getAllRoles() {
        ResponseObject<List<Role>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(lookupService.getAllRoles());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/employmentTypes")
    public ResponseEntity<ResponseObject<List<EmploymentType>>> getAllEmployType() {
        ResponseObject<List<EmploymentType>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(lookupService.getAllEmployType());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/subject")
    public ResponseEntity<ResponseObject<List<Subject>>> getAllSubject(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        int tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<List<Subject>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(lookupService.getAllSubject(tenantId));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/classMaster")
    public ResponseEntity<ResponseObject<List<ClassMaster>>> getAllClassMaster(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        int tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<List<ClassMaster>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(lookupService.getAllClassMaster(tenantId));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/subject/save")
    public ResponseEntity<ResponseObject<Boolean>> saveSubject(HttpServletRequest request, @RequestBody Subject subjectRequest) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<Boolean> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(lookupService.saveSubject(tenantId, subjectRequest));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/classSection")
    public ResponseEntity<ResponseObject<List<ClassSectionResponse>>> getAllClassSection(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtHelper.cleanToken(authHeader);
        int tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<List<ClassSectionResponse>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(lookupService.getAllClassSection(tenantId));

        return ResponseEntity.ok(response);
    }
}
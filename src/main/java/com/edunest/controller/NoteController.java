package com.edunest.controller;

import com.edunest.common.ResponseObject;
import com.edunest.configuration.JwtHelper;
import com.edunest.dto.note.NoteRequest;
import com.edunest.dto.note.NoteResponse;
import com.edunest.service.NoteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/note")
public class NoteController {

    @Autowired
    NoteService noteService;

    @Autowired
    JwtHelper jwtHelper;

    @GetMapping("/list/{classId}")
    public ResponseEntity<ResponseObject<List<NoteResponse>>> getNoteList(
            HttpServletRequest request,
            @PathVariable Integer classId,
            @RequestParam(required = false) Integer sectionId) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<List<NoteResponse>> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(noteService.getNoteList(tenantId, classId, sectionId));
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject<Boolean>> saveNote(
            HttpServletRequest request,
            @RequestPart("data") NoteRequest noteRequest,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer tenantId = jwtHelper.extractTenantId(token);
        Integer loginTeacherId = jwtHelper.extractTeacherId(token);

        ResponseObject<Boolean> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(noteService.saveNote(tenantId, loginTeacherId, noteRequest, file));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<ResponseObject<Boolean>> deleteNote(
            HttpServletRequest request, @PathVariable Integer noteId) {

        String token = jwtHelper.cleanToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        Integer tenantId = jwtHelper.extractTenantId(token);

        ResponseObject<Boolean> response = new ResponseObject<>();
        response.setSuccess(true);
        response.setData(noteService.deleteNote(tenantId, noteId));
        return ResponseEntity.ok(response);
    }
}

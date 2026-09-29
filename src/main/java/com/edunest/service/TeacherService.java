package com.edunest.service;

import com.edunest.dto.teacher.TeacherDTO;
import com.edunest.dto.teacher.TeacherListResponse;

import java.util.List;

public interface TeacherService {
    List<TeacherListResponse> getTeacherList(Integer tenantId, Integer teacherId);

    boolean saveTeacher(Integer teacherId, Integer tenantId, Integer loginTeacherId, TeacherDTO request);

    List<TeacherListResponse> getTeachersBySubject(Integer tenantId, Integer subjectId);

    TeacherDTO getTeacherById(Integer teacherId);

    boolean deleteTeacher(Integer teacherId, Integer loginTeacherId);
}

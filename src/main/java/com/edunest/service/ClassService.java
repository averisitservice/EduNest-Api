package com.edunest.service;

import com.edunest.dto.classes.ClassListResponse;
import com.edunest.dto.classes.ClassDTO;
import com.edunest.entity.Subject;

import java.util.List;

public interface ClassService {
    List<ClassListResponse> getClassList(Integer tenantId);

    ClassDTO getClassById(Integer classId, Integer tenantId);

    boolean saveClass(Integer classId, Integer tenantId, ClassDTO request);

    boolean deleteClass(Integer classId);

    List<Subject> getClassSubjects(Integer classId, Integer tenantId);
}
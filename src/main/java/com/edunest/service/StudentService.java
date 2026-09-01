package com.edunest.service;


import com.edunest.common.PagedResponse;
import com.edunest.dto.student.StudentListResponse;
import com.edunest.dto.student.StudentDTO;

public interface StudentService {
    PagedResponse<StudentListResponse> getStudentList(
            Integer tenantId, int page, int size, String search,
            Integer classId, Integer sectionId, String sortBy, String sortDir);

    StudentDTO getStudentById(Integer studentId, Integer tenantId);

    boolean saveStudent(Integer tenantId, Integer loginTeacherId, StudentDTO request);

    boolean deleteStudent(Integer tenantId, Integer studentId, Integer loginTeacherId);
}
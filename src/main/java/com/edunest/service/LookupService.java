package com.edunest.service;

import com.edunest.dto.classes.ClassSectionResponse;
import com.edunest.entity.ClassMaster;
import com.edunest.entity.EmploymentType;
import com.edunest.entity.Role;
import com.edunest.entity.Subject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LookupService {

    List<Role> getAllRoles();

    List<EmploymentType> getAllEmployType();

    List<Subject> getAllSubject(int tenantId);

    List<ClassMaster> getAllClassMaster(int tenantId);

    boolean saveSubject(Integer tenantId, Subject subject);

    List<ClassSectionResponse> getAllClassSection(int tenantId);
}

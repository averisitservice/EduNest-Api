package com.edunest.service;

import com.edunest.constant.Constant;
import com.edunest.dto.classes.ClassSectionResponse;
import com.edunest.entity.*;
import com.edunest.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LookupServiceImpl implements LookupService {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    EmploymentTypeRepository employmentTypeRepository;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    ClassMasterRepository classMasterRepository;

    @Autowired
    ClassSectionRepository classSectionRepository;

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findByRoleIdNot(Constant.SUPER_ADMIN);
    }

    @Override
    public List<EmploymentType> getAllEmployType() {
        return employmentTypeRepository.findAll();
    }

    @Override
    public List<Subject> getAllSubject(int tenantId) {
        return subjectRepository.findByTenantId(tenantId);
    }

    @Override
    public List<ClassMaster> getAllClassMaster(int tenantId) {
        return classMasterRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    @Override
    public boolean saveSubject(Integer tenantId, Subject request) {
        Subject subject = new Subject();
        subject.setTenantId(tenantId);
        subject.setSubjectName(request.getSubjectName());
        subject.setSubjectCode(request.getSubjectCode());
        subject.setIsActive(true);
        subjectRepository.save(subject);
        return true;
    }


    @Override
    public List<ClassSectionResponse> getAllClassMasterWithSections(int tenantId) {
        List<ClassSectionResponse> responseList = new ArrayList<>();
        List<ClassMaster> classMasters = classMasterRepository.findByTenantIdAndIsActiveTrue(tenantId);

        for (ClassMaster classMaster : classMasters) {
            List<ClassSection> sections = classSectionRepository.findByTenantIdAndClassIdAndIsActiveTrue(tenantId, classMaster.getClassId());

            if (sections.isEmpty()) {
                responseList.add(new ClassSectionResponse(
                        classMaster.getClassId(),
                        classMaster.getClassName(),
                        null,
                        null,
                        classMaster.getIsActive()
                ));
            } else {
                for (ClassSection section : sections) {
                    responseList.add(new ClassSectionResponse(
                            classMaster.getClassId(),
                            classMaster.getClassName(),
                            section.getSectionId(),
                            section.getSectionName(),
                            section.getIsActive()
                    ));
                }
            }
        }
        return responseList;
    }

}

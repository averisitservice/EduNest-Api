package com.edunest.service;

import com.edunest.dto.classes.ClassListResponse;
import com.edunest.dto.classes.ClassDTO;
import com.edunest.entity.*;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ClassServiceImpl implements ClassService {

    @Autowired
    ClassMasterRepository classMasterRepository;

    @Autowired
    ClassSectionRepository classSectionRepository;

    @Autowired
    ClassSubjectRepository classSubjectRepository;

    @Autowired
    StudentClassRepository studentClassRepository;

    @Autowired
    ClassFeeRepository classFeeRepository;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    CommonHelper commonHelper;

    @Override
    public List<ClassListResponse> getClassList(Integer tenantId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);
        List<ClassMaster> classes = classMasterRepository.findByTenantIdAndIsActiveTrue(tenantId);
        List<ClassListResponse> classListResponses = new ArrayList<>();

        for (ClassMaster classMaster : classes) {
            List<ClassSection> classSections = classSectionRepository.findByClassIdAndTenantId(classMaster.getClassId(), tenantId);
            List<String> sectionNames = new ArrayList<>();
            for (ClassSection classSection : classSections) {
                sectionNames.add(classSection.getSectionName());
            }
            List<ClassSubject> classSubjects = classSubjectRepository.findByClassIdAndTenantId(classMaster.getClassId(), tenantId);
            List<String> subjectNames = new ArrayList<>();
            for (ClassSubject classSubject : classSubjects) {
                Subject subject = subjectRepository.findById(classSubject.getSubjectId()).orElse(null);
                if (subject != null) {
                    subjectNames.add(subject.getSubjectName());
                }
            }

            ClassFee classFee = null;
            if (currentYear != null) {
                classFee = classFeeRepository.findByClassIdAndAcademicYearIdAndTenantId(classMaster.getClassId(), currentYear.getAcademicYearId(), tenantId);
            }

            ClassListResponse classListResponse = new ClassListResponse();
            classListResponse.setClassId(classMaster.getClassId());
            classListResponse.setClassName(classMaster.getClassName());
            classListResponse.setIsActive(classMaster.getIsActive());
            classListResponse.setAnnualFee(classFee != null ? classFee.getAnnualFee() : null);
            classListResponse.setHostelFee(classFee != null ? classFee.getHostelFee() : null);
            classListResponse.setSections(sectionNames);
            classListResponse.setSubjects(subjectNames);
            classListResponses.add(classListResponse);
        }
        return classListResponses;
    }

    @Override
    public ClassDTO getClassById(Integer classId, Integer tenantId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);
        ClassMaster classMaster = classMasterRepository.findById(classId).orElseThrow(() -> new CustomException("Class", "Class not found"));

        List<ClassSection> classSections = classSectionRepository.findByClassIdAndTenantId(classId, tenantId);
        List<String> sectionNames = new ArrayList<>();
        for (ClassSection classSection : classSections) {
            sectionNames.add(classSection.getSectionName());
        }

        List<ClassSubject> classSubjects = classSubjectRepository.findByClassIdAndTenantId(classId, tenantId);
        List<Integer> subjectIds = new ArrayList<>();
        for (ClassSubject classSubject : classSubjects) {
            subjectIds.add(classSubject.getSubjectId());
        }

        ClassFee classFee = null;
        if (currentYear != null) {
            classFee = classFeeRepository.findByClassIdAndAcademicYearIdAndTenantId(classId, currentYear.getAcademicYearId(), tenantId);
        }

        ClassDTO classDTO = new ClassDTO();
        classDTO.setClassName(classMaster.getClassName());
        classDTO.setAnnualFee(classFee != null ? classFee.getAnnualFee() : null);
        classDTO.setHostelFee(classFee != null ? classFee.getHostelFee() : null);
        classDTO.setSections(sectionNames);
        classDTO.setSubjectIds(subjectIds);
        return classDTO;
    }

    @Override
    @Transactional
    public boolean saveClass(Integer classId, Integer tenantId, ClassDTO request) {

        boolean isEdit = (classId != null);
        ClassMaster classMaster;

        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        if (isEdit) {
            classMaster = classMasterRepository.findById(classId).orElseThrow(() -> new CustomException("classId", "Class not found"));
            classMaster.setClassName(request.getClassName());
        } else {
            if (classMasterRepository.existsByClassNameAndTenantId(request.getClassName(), tenantId)) {
                throw new CustomException("className", "Class already exists");
            }
            classMaster = new ClassMaster();
            classMaster.setTenantId(tenantId);
            classMaster.setClassName(request.getClassName());
            classMaster.setIsActive(true);
        }

        ClassMaster savedClass = classMasterRepository.save(classMaster);
        Integer savedClassId = savedClass.getClassId();

        List<ClassSection> existingSections = classSectionRepository.findByClassIdAndTenantId(savedClassId, tenantId);

        List<String> requestedNames = new ArrayList<>();
        if (request.getSections() != null) {
            for (String name : request.getSections()) {
                if (name != null && !name.trim().isEmpty()) {
                    requestedNames.add(name.trim());
                }
            }
        }

        Set<String> requestedSet = new HashSet<>(requestedNames);
        Set<String> existingNames = new HashSet<>();
        for (ClassSection classSection : existingSections) {
            existingNames.add(classSection.getSectionName().trim());
            if (!requestedSet.contains(classSection.getSectionName().trim())) {
                boolean hasStudents = studentClassRepository.existsBySectionIdAndTenantId(classSection.getSectionId(), tenantId);
                if (!hasStudents) {
                    classSectionRepository.delete(classSection);
                }
            }
        }

        for (String name : requestedNames) {
            if (!existingNames.contains(name)) {
                ClassSection classSection = new ClassSection();
                classSection.setTenantId(tenantId);
                classSection.setClassId(savedClassId);
                classSection.setSectionName(name);
                classSection.setIsActive(true);
                classSectionRepository.save(classSection);
            }
        }

        if (request.getSubjectIds() != null && !request.getSubjectIds().isEmpty()) {
            if (isEdit) {
                List<ClassSubject> oldSubjects = classSubjectRepository.findByClassIdAndTenantId(savedClassId, tenantId);
                classSubjectRepository.deleteAll(oldSubjects);
            }
            for (Integer subjectId : request.getSubjectIds()) {
                ClassSubject classSubject = new ClassSubject();
                classSubject.setTenantId(tenantId);
                classSubject.setClassId(savedClassId);
                classSubject.setSubjectId(subjectId);
                classSubject.setAcademicYearId(currentYear.getAcademicYearId());
                classSubject.setIsActive(true);
                classSubjectRepository.save(classSubject);
            }
        }

        if (request.getAnnualFee() != null) {
            ClassFee classFee = classFeeRepository.findByClassIdAndAcademicYearIdAndTenantId(savedClassId, currentYear.getAcademicYearId(), tenantId);
            if (classFee == null) {
                classFee = new ClassFee();
                classFee.setTenantId(tenantId);
                classFee.setClassId(savedClassId);
                classFee.setAcademicYearId(currentYear.getAcademicYearId());
                classFee.setIsActive(true);
            }
            classFee.setAnnualFee(request.getAnnualFee());
            classFee.setHostelFee(request.getHostelFee());
            classFeeRepository.save(classFee);
        }
        return true;
    }


    @Override
    public boolean deleteClass(Integer classId) {
        ClassMaster classMaster = classMasterRepository.findById(classId).orElseThrow(() -> new CustomException("classId", "Class not found"));
        classMaster.setIsActive(false);
        classMasterRepository.save(classMaster);
        return true;
    }

    @Override
    public List<Subject> getClassSubjects(Integer classId, Integer tenantId) {
        List<ClassSubject> classSubjects = classSubjectRepository.findByClassIdAndTenantId(classId, tenantId);
        List<Subject> subjects = new ArrayList<>();
        for (ClassSubject classSubject : classSubjects) {
            if (Boolean.FALSE.equals(classSubject.getIsActive())) continue;
            Subject subject = subjectRepository.findById(classSubject.getSubjectId()).orElse(null);
            subjects.add(subject);
        }
        return subjects;
    }
}
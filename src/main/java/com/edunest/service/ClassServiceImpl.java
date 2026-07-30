package com.edunest.service;

import com.edunest.dto.classes.ClassListResponse;
import com.edunest.dto.classes.ClassRequest;
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
    AcademicYearRepository academicYearRepository;

    @Autowired
    CommonHelper commonHelper;

    @Override
    public List<ClassListResponse> getClassList(Integer tenantId) {
        List<ClassMaster> classes = classMasterRepository.findByTenantIdAndIsActiveTrue(tenantId);
        AcademicYear currentYear = academicYearRepository.findByTenantIdAndIsCurrentTrue(tenantId);
        List<ClassListResponse> responseList = new ArrayList<>();

        for (ClassMaster classMaster : classes) {
            List<ClassSection> classSections = classSectionRepository.findByClassIdAndTenantId(classMaster.getClassId(), tenantId);
            List<String> sectionNames = new ArrayList<>();
            for (ClassSection cs : classSections) {
                sectionNames.add(cs.getSectionName());
            }
            List<ClassSubject> classSubjects = classSubjectRepository.findByClassIdAndTenantId(classMaster.getClassId(), tenantId);
            List<String> subjectNames = new ArrayList<>();
            for (ClassSubject cs : classSubjects) {
                Subject subject = subjectRepository.findById(cs.getSubjectId()).orElse(null);
                if (subject != null) {
                    subjectNames.add(subject.getSubjectName());
                }
            }

            ClassFee classFee = null;
            if (currentYear != null) {
                classFee = classFeeRepository.findByClassIdAndAcademicYearIdAndTenantId(classMaster.getClassId(), currentYear.getAcademicYearId(), tenantId);
            }

            ClassListResponse response = new ClassListResponse();
            response.setClassId(classMaster.getClassId());
            response.setClassName(classMaster.getClassName());
            response.setIsActive(classMaster.getIsActive());
            response.setAnnualFee(classFee != null ? classFee.getAnnualFee() : null);
            response.setHostelFee(classFee != null ? classFee.getHostelFee() : null);
            response.setSections(sectionNames);
            response.setSubjects(subjectNames);
            responseList.add(response);
        }
        return responseList;
    }

    @Override
    public ClassRequest getClassById(Integer classId, Integer tenantId) {
        ClassMaster classMaster = classMasterRepository.findById(classId).orElseThrow(() -> new CustomException("Class", "Class not found"));

        AcademicYear currentYear = academicYearRepository.findByTenantIdAndIsCurrentTrue(tenantId);

        List<ClassSection> classSections = classSectionRepository.findByClassIdAndTenantId(classId, tenantId);
        List<String> sectionNames = new ArrayList<>();
        for (ClassSection cs : classSections) {
            sectionNames.add(cs.getSectionName());
        }

        List<ClassSubject> classSubjects = classSubjectRepository.findByClassIdAndTenantId(classId, tenantId);
        List<Integer> subjectIds = new ArrayList<>();
        for (ClassSubject cs : classSubjects) {
            subjectIds.add(cs.getSubjectId());
        }

        ClassFee classFee = null;
        if (currentYear != null) {
            classFee = classFeeRepository.findByClassIdAndAcademicYearIdAndTenantId(classId, currentYear.getAcademicYearId(), tenantId);
        }

        ClassRequest request = new ClassRequest();
        request.setClassName(classMaster.getClassName());
        request.setAnnualFee(classFee != null ? classFee.getAnnualFee() : null);
        request.setHostelFee(classFee != null ? classFee.getHostelFee() : null);
        request.setSections(sectionNames);
        request.setSubjectIds(subjectIds);
        return request;
    }

    @Override
    @Transactional
    public boolean saveClass(Integer classId, Integer tenantId, ClassRequest request) {

        boolean isEdit = (classId != null);
        ClassMaster classMaster;

        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);
        Integer academicYearId = currentYear.getAcademicYearId();

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
        for (ClassSection cs : existingSections) {
            existingNames.add(cs.getSectionName().trim());
            if (!requestedSet.contains(cs.getSectionName().trim())) {
                boolean hasStudents = studentClassRepository.existsBySectionIdAndTenantId(cs.getSectionId(), tenantId);
                if (!hasStudents) {
                    classSectionRepository.delete(cs);
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
                classSubject.setAcademicYearId(academicYearId);
                classSubject.setIsActive(true);
                classSubjectRepository.save(classSubject);
            }
        }

        if (request.getAnnualFee() != null) {
            ClassFee classFee = classFeeRepository.findByClassIdAndAcademicYearIdAndTenantId(savedClassId, academicYearId, tenantId);
            if (classFee == null) {
                classFee = new ClassFee();
                classFee.setTenantId(tenantId);
                classFee.setClassId(savedClassId);
                classFee.setAcademicYearId(academicYearId);
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
        for (ClassSubject cs : classSubjects) {
            if (Boolean.FALSE.equals(cs.getIsActive())) continue;
            Subject subject = subjectRepository.findById(cs.getSubjectId()).orElse(null);
            subjects.add(subject);
        }
        return subjects;
    }
}
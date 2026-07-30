package com.edunest.service;


import com.edunest.common.PagedResponse;
import com.edunest.dto.student.StudentListResponse;
import com.edunest.dto.student.StudentRequest;
import com.edunest.entity.*;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.helper.CryptoHelper;
import com.edunest.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    StudentClassRepository studentClassRepository;

    @Autowired
    ClassSectionRepository classSectionRepository;

    @Autowired
    ClassMasterRepository classMasterRepository;

    @Autowired
    TeacherRepository teacherRepository;

    @Autowired
    CommonHelper commonHelper;

    @Override
    public PagedResponse<StudentListResponse> getStudentList(
            Integer tenantId, int page, int size, String search,
            Integer classId, Integer sectionId, String sortBy, String sortDir) {

        String normalizedSearch = (search != null && !search.isBlank()) ? search.trim().toLowerCase() : "";

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortProperty = mapSortProperty(sortBy);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));

        Page<Student> studentPage = studentRepository.searchStudents(tenantId, normalizedSearch, classId, sectionId, pageable);

        List<StudentListResponse> content = new ArrayList<>();
        for (Student student : studentPage.getContent()) {
            content.add(toResponse(student, tenantId));
        }

        return new PagedResponse<>(
                content,
                studentPage.getTotalElements(),
                studentPage.getTotalPages(),
                studentPage.getNumber(),
                studentPage.getSize());
    }

    private String mapSortProperty(String sortBy) {
        if (sortBy == null) return "updatedDate";
        return switch (sortBy) {
            case "studentName" -> "firstName";
            case "mobileNo" -> "mobileNo";
            case "updatedDate" -> "updatedDate";
            default -> "updatedDate";
        };
    }

    private StudentListResponse toResponse(Student student, Integer tenantId) {
        StudentClass studentClass = studentClassRepository.findByStudentIdAndTenantId(student.getStudentId(), tenantId).orElse(null);

        String className = null;
        String sectionName = null;
        String displayClass = null;
        String rollNo = null;

        if (studentClass != null) {
            ClassMaster classMaster = classMasterRepository.findById(studentClass.getClassId()).orElse(null);
            ClassSection classSection = null;
            if (studentClass.getSectionId() != null) {
                classSection = classSectionRepository.findById(studentClass.getSectionId()).orElse(null);
            }
            className = classMaster != null ? classMaster.getClassName() : null;
            sectionName = classSection != null ? classSection.getSectionName() : null;
            displayClass = (className != null && sectionName != null) ? className + " - " + sectionName : className;
            rollNo = studentClass.getRollNo();
        }

        String updatedByName = commonHelper.teacherName(student.getUpdatedBy());

        StudentListResponse response = new StudentListResponse();
        response.setStudentId(student.getStudentId());
        response.setAdmissionNo(student.getAdmissionNo());
        response.setStudentName(student.getFirstName() + " " + student.getLastName());
        response.setGender(student.getGender());
        response.setDateOfBirth(student.getDateOfBirth());
        response.setMobileNo(student.getMobileNo());
        response.setEmail(student.getEmail());
        response.setClassName(className);
        response.setSectionName(sectionName);
        response.setDisplayClass(displayClass);
        response.setRollNo(rollNo);
        response.setIsActive(student.getIsActive());
        response.setLastLogin(student.getLastLogin());
        response.setFatherName(student.getFatherName());
        response.setParentMobile(student.getParentMobile());
        response.setUpdatedDate(student.getUpdatedDate());
        response.setUpdatedBy(updatedByName);
        response.setIsHostel(student.getIsHostel());
        return response;
    }

    @Override
    public StudentRequest getStudentById(Integer studentId, Integer tenantId) {
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new CustomException("studentId", "Student not found"));

        StudentClass studentClass = studentClassRepository.findByStudentIdAndTenantId(studentId, tenantId).orElse(null);

        StudentRequest request = new StudentRequest();
        request.setStudentId(student.getStudentId());
        request.setFirstName(student.getFirstName());
        request.setMiddleName(student.getMiddleName());
        request.setLastName(student.getLastName());
        request.setGender(student.getGender());
        request.setDateOfBirth(student.getDateOfBirth());
        request.setAadharNo(student.getAadharNo());
        request.setEmail(student.getEmail());
        request.setMobileNo(student.getMobileNo());
        request.setAddressLine1(student.getAddressLine1());
        request.setCity(student.getCity());
        request.setState(student.getState());
        request.setPostalCode(student.getPostalCode());
        request.setFatherName(student.getFatherName());
        request.setMotherName(student.getMotherName());
        request.setParentMobile(student.getParentMobile());
        request.setParentEmail(student.getParentEmail());
        request.setParentAadhar(student.getParentAadhar());

        request.setIsHostel(student.getIsHostel());

        if (studentClass != null) {
            request.setSectionId(studentClass.getSectionId());
            request.setClassId(studentClass.getClassId());
            request.setRollNo(studentClass.getRollNo());
        }
        return request;
    }

    @Override
    @Transactional
    public boolean saveStudent(Integer tenantId, Integer loginTeacherId, StudentRequest request) {

        boolean isEdit = (request.getStudentId() != null);
        Student student;

        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        if (isEdit) {
            student = studentRepository.findById(request.getStudentId()).orElseThrow(() -> new CustomException("studentId", "Student not found"));
        } else {
            student = new Student();
            student.setTenantId(tenantId);
            student.setAdmissionNo(commonHelper.generateAdmissionNo(tenantId));
            String hashKey = CryptoHelper.getHashKey();
            String initialPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                    ? request.getPassword()
                    : request.getMobileNo();

            student.setHashkey(hashKey);
            student.setUsername(CommonHelper.generateUsername(request.getFirstName(), request.getDateOfBirth()));
            student.setPassword(CryptoHelper.encryptPassword(initialPassword, hashKey));
            student.setIsActive(true);
            student.setCreatedBy(loginTeacherId);
        }

        student.setFirstName(request.getFirstName());
        student.setMiddleName(request.getMiddleName());
        student.setLastName(request.getLastName());
        student.setGender(request.getGender());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setAadharNo(request.getAadharNo());
        student.setEmail(request.getEmail());
        student.setMobileNo(request.getMobileNo());
        student.setAddressLine1(request.getAddressLine1());
        student.setCity(request.getCity());
        student.setState(request.getState());
        student.setPostalCode(request.getPostalCode());
        student.setFatherName(request.getFatherName());
        student.setMotherName(request.getMotherName());
        student.setParentMobile(request.getParentMobile());
        student.setParentEmail(request.getParentEmail());
        student.setParentAadhar(request.getParentAadhar());
        student.setIsHostel(request.getIsHostel() != null && request.getIsHostel());
        student.setUpdatedBy(loginTeacherId);
        student.setUpdatedDate(LocalDateTime.now());

        Student savedStudent = studentRepository.save(student);
        Integer savedStudentId = savedStudent.getStudentId();

        if (request.getClassId() != null || request.getSectionId() != null) {
            StudentClass studentClass = studentClassRepository.findByStudentIdAndTenantId(savedStudentId, tenantId).orElse(new StudentClass());

            if (studentClass.getStudentClassId() == null) {
                studentClass.setTenantId(tenantId);
                studentClass.setStudentId(savedStudentId);
                studentClass.setIsActive(true);
            }
            studentClass.setClassId(request.getClassId());
            studentClass.setSectionId(request.getSectionId());
            studentClass.setAcademicYearId(currentYear.getAcademicYearId());
            studentClass.setRollNo(request.getRollNo());

            studentClassRepository.save(studentClass);
        }

        return true;
    }

    @Override
    public boolean deleteStudent(Integer studentId, Integer loginTeacherId) {
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new CustomException("studentId", "Student not found"));
        student.setIsActive(false);
        student.setUpdatedBy(loginTeacherId);
        student.setUpdatedDate(LocalDateTime.now());
        studentRepository.save(student);
        return true;
    }
}
package com.edunest.service;

import com.edunest.common.PagedResponse;
import com.edunest.dto.student.StudentListResponse;
import com.edunest.dto.student.StudentDTO;
import com.edunest.entity.*;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.helper.CryptoHelper;
import com.edunest.repository.ClassMasterRepository;
import com.edunest.repository.ClassSectionRepository;
import com.edunest.repository.StudentClassRepository;
import com.edunest.repository.StudentRepository;
import org.springframework.beans.BeanUtils;
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
        if ("studentName".equals(sortBy)) {
            return "firstName";
        } else if ("mobileNo".equals(sortBy)) {
            return "mobileNo";
        } else {
            return "updatedDate";
        }
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

        String updatedByName = commonHelper.teacherNameForId(student.getUpdatedBy());

        StudentListResponse studentListResponse = new StudentListResponse();
        BeanUtils.copyProperties(student, studentListResponse);
        studentListResponse.setStudentName(CommonHelper.studentNameForStudent(student));
        studentListResponse.setClassName(className);
        studentListResponse.setSectionName(sectionName);
        studentListResponse.setDisplayClass(displayClass);
        studentListResponse.setRollNo(rollNo);
        studentListResponse.setUpdatedBy(updatedByName);
        return studentListResponse;
    }

    @Override
    public StudentDTO getStudentById(Integer studentId, Integer tenantId) {
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new CustomException("studentId", "Student not found"));

        StudentClass studentClass = studentClassRepository.findByStudentIdAndTenantId(studentId, tenantId).orElse(null);

        StudentDTO request = new StudentDTO();
        BeanUtils.copyProperties(student, request, "password");

        if (studentClass != null) {
            request.setSectionId(studentClass.getSectionId());
            request.setClassId(studentClass.getClassId());
            request.setRollNo(studentClass.getRollNo());
        }
        return request;
    }

    @Override
    @Transactional
    public boolean saveStudent(Integer tenantId, Integer loginTeacherId, StudentDTO request) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        // Check for duplicate roll number in the class and section
        if (request.getClassId() != null && request.getRollNo() != null && !request.getRollNo().isBlank()) {
            Integer checkStudentId = request.getStudentId() != null ? request.getStudentId() : -1;
            boolean rollNoExists = studentClassRepository.existsByRollNo(
                    tenantId,
                    request.getClassId(),
                    request.getSectionId(),
                    currentYear.getAcademicYearId(),
                    request.getRollNo().trim(),
                    checkStudentId
            );
            if (rollNoExists) {
                throw new CustomException("rollNo", "Roll number '" + request.getRollNo().trim() + "' already exists in this class/section");
            }
        }

        boolean isEdit = (request.getStudentId() != null);
        Student student;

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
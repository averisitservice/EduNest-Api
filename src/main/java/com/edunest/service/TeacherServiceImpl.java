package com.edunest.service;

import com.edunest.dto.teacher.TeacherClassRequest;
import com.edunest.dto.teacher.TeacherDTO;
import com.edunest.dto.teacher.TeacherListResponse;
import com.edunest.dto.teacher.TeacherSubjectRequest;
import com.edunest.entity.Teacher;
import com.edunest.entity.TeacherClass;
import com.edunest.entity.TeacherSubject;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.helper.CryptoHelper;
import com.edunest.repository.TeacherClassRepository;
import com.edunest.repository.TeacherRepository;
import com.edunest.repository.TeacherSubjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    TeacherRepository teacherRepository;

    @Autowired
    TeacherClassRepository teacherClassRepository;

    @Autowired
    TeacherSubjectRepository teacherSubjectRepository;

    @Autowired
    CommonHelper commonHelper;

    @Override
    public List<TeacherListResponse> getTeacherList(Integer tenantId, Integer teacherId) {
        List<Teacher> teachers = teacherRepository.findByTenantIdAndIsActiveTrueAndTeacherIdNot(tenantId, teacherId);
        List<TeacherListResponse> responseList = new ArrayList<>();

        for (Teacher teacher : teachers) {
            TeacherListResponse response = new TeacherListResponse();
            BeanUtils.copyProperties(teacher, response);

            response.setEmploymentId(teacher.getEmploymentTypeId());
            response.setTeacherName(teacher.getFirstName() + " " + teacher.getLastName());
            response.setUpdatedBy(commonHelper.teacherName(teacher.getUpdatedBy()));

            responseList.add(response);
        }
        return responseList;
    }

    @Override
    public List<TeacherListResponse> getTeachersBySubject(Integer tenantId, Integer subjectId) {
        List<TeacherSubject> teacherSubjects = teacherSubjectRepository.findBySubjectIdAndTenantIdAndIsActiveTrue(subjectId, tenantId);

        List<TeacherListResponse> responseList = new ArrayList<>();
        for (TeacherSubject teacherSubject : teacherSubjects) {
            Teacher teacher = teacherRepository.findById(teacherSubject.getTeacherId()).orElse(null);
            if (teacher == null || !Boolean.TRUE.equals(teacher.getIsActive())) continue;

            TeacherListResponse response = new TeacherListResponse();
            response.setTeacherId(teacher.getTeacherId());
            response.setTeacherName(teacher.getFirstName() + " " + teacher.getLastName());
            responseList.add(response);
        }
        return responseList;
    }

    @Override
    public boolean deleteTeacher(Integer teacherId, Integer loginTeacherId) {
        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(() -> new CustomException("Teacher", "Teacher not found"));
        teacher.setIsActive(false);
        teacher.setUpdatedBy(loginTeacherId);
        teacher.setUpdatedDate(LocalDateTime.now());
        teacherRepository.save(teacher);
        return true;
    }

    @Override
    @Transactional
    public boolean saveTeacher(Integer teacherId, Integer tenantId, Integer loginTeacherId, TeacherDTO request) {

        boolean isEdit = (teacherId != null);
        Teacher teacher;

        if (isEdit) {
            teacher = teacherRepository.findById(teacherId).orElseThrow(() -> new CustomException("Teacher", "Teacher not found"));
            teacher.setRoleId(request.getRoleId());
            teacher.setFirstName(request.getFirstName());
            teacher.setLastName(request.getLastName());
            teacher.setGender(request.getGender());
            teacher.setDateOfBirth(request.getDateOfBirth());
            teacher.setMobileNo(request.getMobileNo());
            teacher.setEmail(request.getEmail());
            teacher.setQualification(request.getQualification());
            teacher.setJoiningDate(request.getJoiningDate());
            teacher.setAddressLine1(request.getAddressLine1());
            teacher.setCity(request.getCity());
            teacher.setState(request.getState());
            teacher.setPostalCode(request.getPostalCode());
            teacher.setEmploymentTypeId(request.getEmploymentTypeId());
            teacher.setUpdatedBy(loginTeacherId);
            teacher.setUpdatedDate(LocalDateTime.now());
        } else {
            teacher = new Teacher();
            teacher.setTenantId(tenantId);
            teacher.setRoleId(request.getRoleId());
            teacher.setHashkey(CryptoHelper.getHashKey());
            teacher.setPassword(CryptoHelper.encryptPassword(request.getPassword(), CryptoHelper.getHashKey()));
            teacher.setFirstName(request.getFirstName());
            teacher.setLastName(request.getLastName());
            teacher.setGender(request.getGender());
            teacher.setDateOfBirth(request.getDateOfBirth());
            teacher.setMobileNo(request.getMobileNo());
            teacher.setEmail(request.getEmail());
            teacher.setQualification(request.getQualification());
            teacher.setJoiningDate(request.getJoiningDate());
            teacher.setAddressLine1(request.getAddressLine1());
            teacher.setCity(request.getCity());
            teacher.setState(request.getState());
            teacher.setPostalCode(request.getPostalCode());
            teacher.setEmploymentTypeId(request.getEmploymentTypeId());
            teacher.setIsActive(true);
            teacher.setCreatedBy(loginTeacherId);
            teacher.setUpdatedBy(loginTeacherId);
            teacher.setUpdatedDate(LocalDateTime.now());
        }

        Teacher savedTeacher = teacherRepository.save(teacher);
        Integer savedTeacherId = savedTeacher.getTeacherId();

        if (request.getTeacherClasses() != null && !request.getTeacherClasses().isEmpty()) {
            // Delete old records on edit
            if (isEdit) {
                List<TeacherClass> oldClasses = teacherClassRepository.findByTeacherIdAndTenantId(savedTeacherId, tenantId);
                teacherClassRepository.deleteAll(oldClasses);
            }
            for (TeacherClassRequest tc : request.getTeacherClasses()) {
                TeacherClass teacherClass = new TeacherClass();
                teacherClass.setTenantId(tenantId);
                teacherClass.setTeacherId(savedTeacherId);
                teacherClass.setClassId(tc.getClassId());
                teacherClass.setSectionId(tc.getSectionId());
                teacherClass.setIsActive(true);
                teacherClassRepository.save(teacherClass);
            }
        }

        if (request.getTeacherSubjects() != null && !request.getTeacherSubjects().isEmpty()) {
            // Delete old records on edit
            if (isEdit) {
                List<TeacherSubject> oldSubjects = teacherSubjectRepository.findByTeacherIdAndTenantId(savedTeacherId, tenantId);
                teacherSubjectRepository.deleteAll(oldSubjects);
            }
            for (TeacherSubjectRequest ts : request.getTeacherSubjects()) {
                TeacherSubject teacherSubject = new TeacherSubject();
                teacherSubject.setTenantId(tenantId);
                teacherSubject.setTeacherId(savedTeacherId);
                teacherSubject.setSubjectId(ts.getSubjectId());
                teacherSubject.setIsActive(true);
                teacherSubjectRepository.save(teacherSubject);
            }
        }
        return true;
    }

    @Override
    public TeacherDTO getTeacherById(Integer teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(() -> new CustomException("Teacher", "Teacher not found"));

        List<TeacherClass> teacherClasses = teacherClassRepository.findByTeacherIdAndTenantId(teacherId, teacher.getTenantId());

        List<TeacherSubject> teacherSubjects = teacherSubjectRepository.findByTeacherIdAndTenantId(teacherId, teacher.getTenantId());

        List<TeacherClassRequest> teacherClassRequests = new ArrayList<>();
        for (TeacherClass tc : teacherClasses) {
            TeacherClassRequest tcRequest = new TeacherClassRequest();
            tcRequest.setClassId(tc.getClassId());
            tcRequest.setSectionId(tc.getSectionId());
            teacherClassRequests.add(tcRequest);
        }

        List<TeacherSubjectRequest> teacherSubjectRequests = new ArrayList<>();
        for (TeacherSubject ts : teacherSubjects) {
            TeacherSubjectRequest tsRequest = new TeacherSubjectRequest();
            tsRequest.setSubjectId(ts.getSubjectId());
            teacherSubjectRequests.add(tsRequest);
        }

        TeacherDTO teacherDTO = new TeacherDTO();
        BeanUtils.copyProperties(teacher, teacherDTO);

        teacherDTO.setTeacherClasses(teacherClassRequests);
        teacherDTO.setTeacherSubjects(teacherSubjectRequests);

        return teacherDTO;
    }
}

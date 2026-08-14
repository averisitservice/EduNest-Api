package com.edunest.service;

import com.edunest.configuration.JwtHelper;
import com.edunest.dto.auth.TenantResponse;
import com.edunest.dto.mobile.StudentChangePasswordRequest;
import com.edunest.dto.mobile.StudentForgotPasswordRequest;
import com.edunest.dto.mobile.StudentLoginRequest;
import com.edunest.dto.mobile.StudentLoginResponse;
import com.edunest.dto.mobile.StudentProfileResponse;
import com.edunest.dto.mobile.StudentResetCredential;
import com.edunest.entity.ClassMaster;
import com.edunest.entity.ClassSection;
import com.edunest.entity.Student;
import com.edunest.entity.StudentClass;
import com.edunest.entity.Tenant;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.helper.CryptoHelper;
import com.edunest.repository.ClassMasterRepository;
import com.edunest.repository.ClassSectionRepository;
import com.edunest.repository.StudentClassRepository;
import com.edunest.repository.StudentRepository;
import com.edunest.repository.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MobileAuthServiceImpl implements MobileAuthService {

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    TenantRepository tenantRepository;

    @Autowired
    StudentClassRepository studentClassRepository;

    @Autowired
    ClassMasterRepository classMasterRepository;

    @Autowired
    ClassSectionRepository classSectionRepository;

    @Autowired
    JwtHelper jwtHelper;

    @Autowired
    EmailService emailService;

    @Autowired
    CommonHelper commonHelper;

    @Override
    public StudentLoginResponse studentLogin(StudentLoginRequest request) {
        Student student = studentRepository.findByUsernameIgnoreCase(request.getUsername().trim())
                .orElseThrow(() -> new CustomException("username", "Invalid username or password"));

        String encryptedPassword = CryptoHelper.encryptPassword(request.getPassword(), student.getHashkey());
        if (!encryptedPassword.equals(student.getPassword())) {
            throw new CustomException("password", "Invalid username or password");
        }

        if (Boolean.FALSE.equals(student.getIsActive())) {
            throw new CustomException("username", "Account is inactive. Please contact your school");
        }

        Tenant tenant = tenantRepository.findById(student.getTenantId())
                .orElseThrow(() -> new CustomException("tenant", "School not found"));

        if (Boolean.FALSE.equals(tenant.getIsActive())) {
            throw new CustomException("tenant", "This school is no longer active");
        }

        student.setLastLogin(LocalDateTime.now());
        studentRepository.save(student);

        String session = jwtHelper.generateStudentAccessToken(student);
        String refresh = jwtHelper.generateStudentRefreshToken(student);

        StudentProfileResponse profile = new StudentProfileResponse();
        profile.setStudentId(student.getStudentId());
        profile.setAdmissionNo(student.getAdmissionNo());
        profile.setStudentName(commonHelper.studentNameForId(student.getStudentId()));
        profile.setEmail(student.getEmail());
        profile.setMobileNo(student.getMobileNo());
        profile.setPhotoUrl(student.getPhotoUrl());

        applyClassPlacement(profile, student);

            TenantResponse tenantResponse = new TenantResponse();
            tenantResponse.setTenantId(tenant.getTenantId());
            tenantResponse.setSchoolCode(tenant.getSchoolCode());
            tenantResponse.setTenantName(tenant.getTenantName());
            tenantResponse.setSchoolBannerUrl(tenant.getSchoolBannerUrl());
            tenantResponse.setMobileLogoUrl(tenant.getMobileLogoUrl());
            tenantResponse.setLogoUrl(tenant.getLogoUrl());
            tenantResponse.setSingleLogoUrl(tenant.getSingleLogoUrl());
            tenantResponse.setPrimaryColor(tenant.getPrimaryColor());
            tenantResponse.setFaviconUrl(tenant.getFaviconUrl());
            tenantResponse.setIsHostel(tenant.getIsHostel());


        return new StudentLoginResponse(session, refresh, profile, tenantResponse);
    }

    @Override
    @Transactional
    public void forgotPassword(StudentForgotPasswordRequest request) {

        String email = request.getEmail().trim();

        List<Student> students = studentRepository.findByEmailIgnoreCaseAndIsActiveTrue(email);
        if (students.isEmpty()) {
            throw new CustomException("email", "No account found with this email");
        }

        List<StudentResetCredential> accounts = new ArrayList<>();

        for (Student student : students) {
            String newPassword = CommonHelper.generateRandomPassword();
            String hashKey = CryptoHelper.getHashKey();

            student.setHashkey(hashKey);
            student.setPassword(CryptoHelper.encryptPassword(newPassword, hashKey));
            studentRepository.save(student);

            accounts.add(new StudentResetCredential(
                    commonHelper.studentNameForId(student.getStudentId()), student.getUsername(), newPassword));
        }

        emailService.sendStudentPasswordResetEmail(email, accounts);
    }

    private void applyClassPlacement(StudentProfileResponse profile, Student student) {
        StudentClass studentClass = studentClassRepository
                .findByStudentIdAndTenantId(student.getStudentId(), student.getTenantId())
                .orElse(null);

        if (studentClass == null) {
            return;
        }

        ClassMaster classMaster = classMasterRepository.findById(studentClass.getClassId()).orElse(null);
        String className = classMaster != null ? classMaster.getClassName() : null;

        String sectionName = null;
        if (studentClass.getSectionId() != null) {
            ClassSection classSection = classSectionRepository.findById(studentClass.getSectionId()).orElse(null);
            sectionName = classSection != null ? classSection.getSectionName() : null;
        }

        profile.setDisplayClass(
                (className != null && sectionName != null) ? className + " - " + sectionName : className);
        profile.setRollNo(studentClass.getRollNo());
    }

    @Override
    @Transactional
    public void changePassword(Integer studentId, StudentChangePasswordRequest request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException("student", "Student not found"));

        String encryptedCurrent = CryptoHelper.encryptPassword(request.getCurrentPassword(), student.getHashkey());
        if (!encryptedCurrent.equals(student.getPassword())) {
            throw new CustomException("currentPassword", "Current password is incorrect");
        }

        String hashKey = CryptoHelper.getHashKey();
        student.setHashkey(hashKey);
        student.setPassword(CryptoHelper.encryptPassword(request.getNewPassword().trim(), hashKey));
        studentRepository.save(student);
    }
}

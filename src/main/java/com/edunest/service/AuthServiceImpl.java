package com.edunest.service;

import com.edunest.configuration.JwtHelper;
import com.edunest.dto.auth.*;
import com.edunest.dto.teacher.TeacherResponse;
import com.edunest.entity.Teacher;
import com.edunest.entity.Tenant;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.helper.CryptoHelper;
import com.edunest.repository.TeacherRepository;
import com.edunest.repository.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    TeacherRepository teacherRepository;

    @Autowired
    TenantRepository tenantRepository;

    @Autowired
    JwtHelper jwtHelper;

    @Autowired
    EmailService emailService;


    @Override
    public SchoolLookupResponse getTenantBySchoolCode(String schoolCode) {
        Tenant tenant = tenantRepository.findBySchoolCodeIgnoreCaseAndIsActiveTrue(schoolCode.trim())
                .orElseThrow(() -> new CustomException("schoolCode", "Invalid school code"));

        SchoolLookupResponse schoolLookupResponse = new SchoolLookupResponse();
        schoolLookupResponse.setTenantId(tenant.getTenantId());
        schoolLookupResponse.setSchoolCode(tenant.getSchoolCode());
        schoolLookupResponse.setTenantName(tenant.getTenantName());
        schoolLookupResponse.setSchoolBannerUrl(tenant.getSchoolBannerUrl());
        schoolLookupResponse.setMobileLogoUrl(tenant.getMobileLogoUrl());
        schoolLookupResponse.setLogoUrl(tenant.getLogoUrl());
        schoolLookupResponse.setSingleLogoUrl(tenant.getSingleLogoUrl());
        schoolLookupResponse.setPrimaryColor(tenant.getPrimaryColor());
        schoolLookupResponse.setFaviconUrl(tenant.getFaviconUrl());
        schoolLookupResponse.setCity(tenant.getCity());
        schoolLookupResponse.setState(tenant.getState());
        schoolLookupResponse.setIsHostel(tenant.getIsHostel());

        return schoolLookupResponse;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {

        Teacher teacher = teacherRepository.findByEmail(loginRequest.getEmail()).
                orElseThrow(() -> new CustomException("Teacher", "Teacher not found"));

        if (!teacher.getIsActive()) {
            throw new CustomException("Teacher", "Account is inactive. Please contact admin");
        }

        Tenant tenant = tenantRepository.findById(teacher.getTenantId()).orElseThrow(() -> new CustomException("Teacher", "Tenant not found"));

        String encryptedPassword = CryptoHelper.encryptPassword(loginRequest.getPassword(), teacher.getHashkey());
        if (!encryptedPassword.equals(teacher.getPassword())) {
            throw new CustomException("Teacher", "Invalid email or password");
        }

        teacher.setLastLogin(LocalDateTime.now());
        teacherRepository.save(teacher);


        TenantResponse tenantResponse = new TenantResponse();
        BeanUtils.copyProperties(tenant, tenantResponse);

        TeacherResponse teacherResponse = new TeacherResponse();
        teacherResponse.setTeacherId(teacher.getTeacherId());
        teacherResponse.setTeacherName(teacherResponse.getTeacherName());
        teacherResponse.setEmail(teacher.getEmail());
        teacherResponse.setRoleId(teacher.getRoleId());
        teacherResponse.setEmail(teacher.getEmail());

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setSession(jwtHelper.generateAccessToken(teacher));
        loginResponse.setRefresh(jwtHelper.generateRefreshToken(teacher));
        loginResponse.setTeacher(teacherResponse);
        loginResponse.setTenant(tenantResponse);

        return loginResponse;
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {

        Teacher teacher = teacherRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("Teacher", "No account found with this email"));

        if (!teacher.getIsActive()) {
            throw new CustomException("Teacher", "Account is inactive. Please contact admin");
        }

        String newPassword = CommonHelper.generateRandomPassword();

        String hashKey = CryptoHelper.getHashKey();

        teacher.setHashkey(hashKey);
        teacher.setPassword(CryptoHelper.encryptPassword(newPassword, hashKey));
        teacherRepository.save(teacher);

        String teacherName = CommonHelper.teacherNameForTeacher(teacher);
        emailService.sendPasswordResetEmail(teacher.getEmail(), teacherName, newPassword);

    }

    @Override
    public void resetPassword(Integer teacherId, ResetPasswordRequest request) {

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new CustomException("Teacher", "Teacher not found"));

        String encryptedOldPassword = CryptoHelper.encryptPassword(request.getOldPassword(), teacher.getHashkey());
        if (!encryptedOldPassword.equals(teacher.getPassword())) {
            throw new CustomException("oldPassword", "Old password is incorrect");
        }

        if (request.getNewPassword() == null || request.getNewPassword().trim().length() < 8) {
            throw new CustomException("newPassword", "New password must be at least 8 characters");
        }

        String hashKey = CryptoHelper.getHashKey();
        teacher.setHashkey(hashKey);
        teacher.setPassword(CryptoHelper.encryptPassword(request.getNewPassword(), hashKey));
        teacherRepository.save(teacher);

    }

    @Override
    public RenewSessionResponse renewSession(RenewSessionRequest request) {

        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new CustomException("Teacher", "Teacher not found"));

        String newSession = jwtHelper.renewSessionJwt(teacher, request.getRefreshToken());
        return new RenewSessionResponse(new RenewSessionResponse.Token(newSession));
    }
}
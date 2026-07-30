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

        if (schoolCode == null || schoolCode.trim().isEmpty()) {
            throw new CustomException("schoolCode", "Please enter a school code");
        }

        Tenant tenant = tenantRepository.findBySchoolCodeIgnoreCaseAndIsActiveTrue(schoolCode.trim())
                .orElseThrow(() -> new CustomException("schoolCode", "Invalid school code"));

        SchoolLookupResponse response = new SchoolLookupResponse();
        response.setTenantId(tenant.getTenantId());
        response.setSchoolCode(tenant.getSchoolCode());
        response.setTenantName(tenant.getTenantName());
        response.setSchoolBannerUrl(tenant.getSchoolBannerUrl());
        response.setMobileLogoUrl(tenant.getMobileLogoUrl());
        response.setLogoUrl(tenant.getLogoUrl());
        response.setSingleLogoUrl(tenant.getSingleLogoUrl());
        response.setPrimaryColor(tenant.getPrimaryColor());
        response.setFaviconUrl(tenant.getFaviconUrl());
        response.setCity(tenant.getCity());
        response.setState(tenant.getState());
        response.setIsHostel(tenant.getIsHostel());

        return response;
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

        String session = jwtHelper.generateAccessToken(teacher);
        String refresh = jwtHelper.generateRefreshToken(teacher);

        TenantResponse response = new TenantResponse();
        response.setTenantId(tenant.getTenantId());
        response.setSchoolCode(tenant.getSchoolCode());
        response.setFaviconUrl(tenant.getFaviconUrl());
        response.setSchoolBannerUrl(tenant.getSchoolBannerUrl());
        response.setMobileLogoUrl(tenant.getMobileLogoUrl());
        response.setLogoUrl(tenant.getLogoUrl());
        response.setTenantName(tenant.getTenantName());
        response.setSingleLogoUrl(tenant.getSingleLogoUrl());
        response.setPrimaryColor(tenant.getPrimaryColor());
        response.setFaviconUrl(tenant.getFaviconUrl());
        response.setIsHostel(tenant.getIsHostel());

        TeacherResponse teacherResponse = new TeacherResponse();
        teacherResponse.setTeacherId(teacher.getTeacherId());
        teacherResponse.setTeacherName(teacherResponse.getTeacherName());
        teacherResponse.setEmail(teacher.getEmail());
        teacherResponse.setRoleId(teacher.getRoleId());
        teacherResponse.setEmail(teacher.getEmail());

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setSession(session);
        loginResponse.setRefresh(refresh);
        loginResponse.setTeacher(teacherResponse);
        loginResponse.setTenant(response);

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

        String teacherName = (teacher.getFirstName() != null ? teacher.getFirstName() : "") +
                (teacher.getLastName() != null ? " " + teacher.getLastName() : "");
        emailService.sendPasswordResetEmail(teacher.getEmail(), teacherName.trim(), newPassword);

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

    public RenewSessionResponse renewSession(RenewSessionRequest request) {

        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new CustomException("Teacher", "Teacher not found"));

        String newSession = jwtHelper.renewSessionJwt(teacher, request.getRefreshToken());

        return new RenewSessionResponse(new RenewSessionResponse.Token(newSession));
    }
}
package com.edunest.service;

import com.edunest.constant.Constant;
import com.edunest.dto.announcement.AnnouncementRequest;
import com.edunest.dto.announcement.AnnouncementResponse;
import com.edunest.entity.AcademicYear;
import com.edunest.entity.Announcement;
import com.edunest.entity.ClassMaster;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.AnnouncementRepository;
import com.edunest.repository.ClassMasterRepository;
import com.edunest.repository.StudentClassRepository;
import com.edunest.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    AnnouncementRepository announcementRepository;

    @Autowired
    ClassMasterRepository classMasterRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    StudentClassRepository studentClassRepository;

    @Autowired
    StudentNotificationService studentNotificationService;

    @Autowired
    CommonHelper commonHelper;

    @Override
    public List<AnnouncementResponse> getAnnouncements(Integer tenantId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        List<Announcement> announcements = announcementRepository
                .findByTenantIdAndAcademicYearIdAndIsActiveTrueOrderByPublishDateDescAnnouncementIdDesc(
                        tenantId, currentYear.getAcademicYearId());

        List<AnnouncementResponse> announcementResponses = new ArrayList<>();
        for (Announcement announcement : announcements) {
            List<Integer> classIds = parseClassIds(announcement.getClassIds());
            List<String> classNames = new ArrayList<>();
            if (!classIds.isEmpty()) {
                for (ClassMaster classMaster : classMasterRepository.findAllById(classIds)) {
                    classNames.add(classMaster.getClassName());
                }
            }

            AnnouncementResponse response = new AnnouncementResponse();
            response.setAnnouncementId(announcement.getAnnouncementId());
            response.setTitle(announcement.getTitle());
            response.setMessage(announcement.getMessage());
            response.setAudience(announcement.getAudience());
            response.setClassIds(classIds);
            response.setClassNames(classNames);
            response.setPublishDate(announcement.getPublishDate());
            response.setStatus(announcement.getStatus());
            response.setCreatedBy(commonHelper.teacherName(announcement.getCreatedBy()));
            response.setUpdatedBy(commonHelper.teacherName(announcement.getUpdatedBy()));
            response.setUpdatedDate(announcement.getUpdatedDate());
            announcementResponses.add(response);
        }
        return announcementResponses;
    }

    @Override
    @Transactional
    public boolean saveAnnouncement(Integer tenantId, Integer loginTeacherId, AnnouncementRequest request) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        Announcement announcement;
        if (request.getAnnouncementId() != null) {
            announcement = announcementRepository.findById(request.getAnnouncementId())
                    .orElseThrow(() -> new CustomException("announcementId", "Announcement not found"));
        } else {
            announcement = new Announcement();
            announcement.setTenantId(tenantId);
            announcement.setAcademicYearId(currentYear.getAcademicYearId());
            announcement.setIsActive(true);
            announcement.setCreatedBy(loginTeacherId);
        }

        announcement.setTitle(request.getTitle());
        announcement.setMessage(request.getMessage());
        announcement.setAudience(request.getAudience() != null ? request.getAudience() : Constant.All);
        announcement.setClassIds(joinClassIds(request.getClassIds()));
        boolean publishNow = !("SCHEDULED".equalsIgnoreCase(request.getPublishMode()) && request.getPublishDate() != null);
        if (publishNow) {
            announcement.setPublishDate(LocalDate.now());
            announcement.setStatus("PUBLISHED");
        } else {
            announcement.setPublishDate(request.getPublishDate());
            announcement.setStatus("SCHEDULED");
        }
        announcement.setUpdatedBy(loginTeacherId);
        announcement.setUpdatedDate(LocalDateTime.now());
        announcementRepository.save(announcement);

        if (publishNow) {
            sendAnnouncementPush(announcement);
        }
        return true;
    }

    @Override
    public void sendAnnouncementPush(Announcement announcement) {
        List<Integer> studentIds = resolveAudience(announcement);
        if (studentIds.isEmpty()) {
            return;
        }

        studentNotificationService.notify(announcement.getTenantId(), studentIds, "ANNOUNCEMENT",
                announcement.getAnnouncementId(), announcement.getTitle(), announcement.getMessage());
    }

    private List<Integer> resolveAudience(Announcement announcement) {
        if (Constant.All.equalsIgnoreCase(announcement.getAudience())
                || announcement.getClassIds() == null || announcement.getClassIds().isBlank()) {
            return studentRepository.findActiveStudentIds(announcement.getTenantId());
        }
        return studentClassRepository.findStudentIdsByClassIds(
                announcement.getTenantId(), announcement.getAcademicYearId(), parseClassIds(announcement.getClassIds()));
    }

    @Override
    public boolean deleteAnnouncement(Integer tenantId, Integer announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new CustomException("announcementId", "Announcement not found"));
        announcement.setIsActive(false);
        announcementRepository.save(announcement);
        return true;
    }

    private List<Integer> parseClassIds(String classIds) {
        List<Integer> result = new ArrayList<>();
        if (classIds == null || classIds.isBlank()) {
            return result;
        }
        for (String id : classIds.split(",")) {
            String trimmedId = id.trim();
            if (!trimmedId.isEmpty()) {
                result.add(Integer.valueOf(trimmedId));
            }
        }
        return result;
    }

    private String joinClassIds(List<Integer> classIds) {
        if (classIds == null || classIds.isEmpty()) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (Integer classId : classIds) {
            if (result.length() > 0) {
                result.append(",");
            }
            result.append(classId);
        }
        return result.toString();
    }
}

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
    CommonHelper commonHelper;

    @Override
    public List<AnnouncementResponse> getAnnouncements(Integer tenantId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        List<Announcement> announcements = announcementRepository
                .findByTenantIdAndAcademicYearIdAndIsActiveTrueOrderByPublishDateDescAnnouncementIdDesc(
                        tenantId, currentYear.getAcademicYearId());

        List<AnnouncementResponse> announcementResponses = new ArrayList<>();
        for (Announcement announcement : announcements) {
            String className = null;
            if (announcement.getClassId() != null) {
                ClassMaster classMaster = classMasterRepository.findById(announcement.getClassId()).orElse(null);
                className = classMaster != null ? classMaster.getClassName() : null;
            }

            AnnouncementResponse response = new AnnouncementResponse();
            response.setAnnouncementId(announcement.getAnnouncementId());
            response.setTitle(announcement.getTitle());
            response.setMessage(announcement.getMessage());
            response.setAudience(announcement.getAudience());
            response.setClassId(announcement.getClassId());
            response.setClassName(className);
            response.setPublishDate(announcement.getPublishDate());
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
        announcement.setClassId(request.getClassId());
        announcement.setPublishDate(request.getPublishDate() != null ? request.getPublishDate() : LocalDate.now());
        announcement.setUpdatedBy(loginTeacherId);
        announcement.setUpdatedDate(LocalDateTime.now());
        announcementRepository.save(announcement);
        return true;
    }

    @Override
    public boolean deleteAnnouncement(Integer tenantId, Integer announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new CustomException("announcementId", "Announcement not found"));
        announcement.setIsActive(false);
        announcementRepository.save(announcement);
        return true;
    }
}

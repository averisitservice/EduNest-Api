package com.edunest.service;

import com.edunest.dto.homework.HomeworkRequest;
import com.edunest.dto.homework.HomeworkResponse;
import com.edunest.entity.AcademicYear;
import com.edunest.entity.Homework;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.HomeworkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HomeworkServiceImpl implements HomeworkService {

    private static final String ATTACHMENT_FOLDER = "edunest/homework";

    @Autowired
    HomeworkRepository homeworkRepository;

    @Autowired
    CommonHelper commonHelper;

    @Autowired
    FileStorageService fileStorageService;

    @Override
    public List<HomeworkResponse> getHomeWorkList(Integer tenantId, Integer classId, Integer sectionId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        List<Homework> homeworkList = homeworkRepository.findList(tenantId, currentYear.getAcademicYearId(), classId, sectionId);

        List<HomeworkResponse> homeworkResponseList = new ArrayList<>();
        for (Homework homework : homeworkList) {
            HomeworkResponse homeworkResponse = new HomeworkResponse();
            homeworkResponse.setHomeworkId(homework.getHomeworkId());
            homeworkResponse.setClassId(homework.getClassId());
            homeworkResponse.setSectionId(homework.getSectionId());
            homeworkResponse.setSubjectId(homework.getSubjectId());
            homeworkResponse.setSubjectName(commonHelper.subjectName(homework.getSubjectId()));
            homeworkResponse.setTitle(homework.getTitle());
            homeworkResponse.setDescription(homework.getDescription());
            homeworkResponse.setDueDate(homework.getDueDate());
            homeworkResponse.setAttachmentUrl(homework.getAttachmentUrl());
            homeworkResponse.setCreatedBy(commonHelper.teacherName(homework.getCreatedBy()));
            homeworkResponse.setUpdatedBy(commonHelper.teacherName(homework.getUpdatedBy()));
            homeworkResponse.setUpdatedDate(homework.getUpdatedDate());
            homeworkResponseList.add(homeworkResponse);
        }
        return homeworkResponseList;
    }

    @Override
    @Transactional
    public boolean saveHomeWork(Integer tenantId, Integer loginTeacherId, HomeworkRequest request, MultipartFile file) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        Homework homework;
        if (request.getHomeworkId() != null) {
            homework = homeworkRepository.findById(request.getHomeworkId())
                    .orElseThrow(() -> new CustomException("homeworkId", "Item not found"));
        } else {
            homework = new Homework();
            homework.setTenantId(tenantId);
            homework.setAcademicYearId(currentYear.getAcademicYearId());
            homework.setIsActive(true);
            homework.setCreatedBy(loginTeacherId);
        }

        homework.setClassId(request.getClassId());
        homework.setSectionId(request.getSectionId());
        homework.setSubjectId(request.getSubjectId());
        homework.setTitle(request.getTitle());
        homework.setDescription(request.getDescription());
        homework.setDueDate(request.getDueDate());

        if (file != null && !file.isEmpty()) {
            Map<String, Object> uploadResult = fileStorageService.uploadFile(file, ATTACHMENT_FOLDER);
            homework.setAttachmentUrl(String.valueOf(uploadResult.get("secure_url")));
        } else if (request.getHomeworkId() == null) {
            homework.setAttachmentUrl(request.getAttachmentUrl());
        }

        homework.setUpdatedBy(loginTeacherId);
        homework.setUpdatedDate(LocalDateTime.now());
        homeworkRepository.save(homework);
        return true;
    }

    @Override
    public boolean deleteHomeWork(Integer tenantId, Integer homeworkId) {
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new CustomException("homeworkId", "Item not found"));
        homework.setIsActive(false);
        homeworkRepository.save(homework);
        return true;
    }
}

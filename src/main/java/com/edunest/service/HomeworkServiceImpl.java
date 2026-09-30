package com.edunest.service;

import com.edunest.configuration.AwsConfiguration;
import com.edunest.constant.Constant;
import com.edunest.dto.homework.HomeworkRequest;
import com.edunest.dto.homework.HomeworkResponse;
import com.edunest.entity.AcademicYear;
import com.edunest.entity.Homework;
import com.edunest.error.CustomException;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.HomeworkRepository;
import com.edunest.repository.StudentClassRepository;
import com.edunest.util.FileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class HomeworkServiceImpl implements HomeworkService {

    private static final String ATTACHMENT_FOLDER = "edunest/homework";

    @Autowired
    HomeworkRepository homeworkRepository;

    @Autowired
    CommonHelper commonHelper;

    @Autowired
    AwsConfiguration awsConfiguration;

    @Autowired
    StudentClassRepository studentClassRepository;

    @Autowired
    StudentNotificationService studentNotificationService;

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
            if (homework.getAttachmentUrl() != null && !homework.getAttachmentUrl().isEmpty()) {
                homeworkResponse.setAttachmentUrl(awsConfiguration.getPresignedUrl(homework.getAttachmentUrl()));
            } else {
                homeworkResponse.setAttachmentUrl(null);
            }
            homeworkResponse.setCreatedBy(commonHelper.teacherNameForId(homework.getCreatedBy()));
            homeworkResponse.setUpdatedBy(commonHelper.teacherNameForId(homework.getUpdatedBy()));
            homeworkResponse.setUpdatedDate(homework.getUpdatedDate());
            homeworkResponseList.add(homeworkResponse);
        }
        return homeworkResponseList;
    }

    @Override
    @Transactional
    public boolean saveHomeWork(Integer tenantId, Integer loginTeacherId, HomeworkRequest request, MultipartFile file) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        boolean isEdit = request.getHomeworkId() != null;
        Homework homework;
        if (isEdit) {
            homework = homeworkRepository.findById(request.getHomeworkId()).orElseThrow(() -> new CustomException("homeworkId", "Item not found"));
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
            String filename = FileHandler.generateUniqueS3Key(file);
            if (isEdit && homework.getAttachmentUrl() != null && !homework.getAttachmentUrl().isEmpty()) {
                awsConfiguration.updateFile(filename, FileHandler.convertMultipartFileToFile(file));
            } else {
                awsConfiguration.uploadFile(filename, FileHandler.convertMultipartFileToFile(file));
            }
            homework.setAttachmentUrl(filename);
        } else if (!isEdit) {
            homework.setAttachmentUrl(request.getAttachmentUrl());
        } else if (request.getAttachmentUrl() != null) {
            homework.setAttachmentUrl(request.getAttachmentUrl());
        }
        homework.setUpdatedBy(loginTeacherId);
        homework.setUpdatedDate(LocalDateTime.now());
        homeworkRepository.save(homework);

        sendHomeworkPush(homework, isEdit);
        return true;
    }

    private void sendHomeworkPush(Homework homework, boolean isEdit) {
        List<Integer> studentIds = studentClassRepository.findStudentIdsByClassAndSection(
                homework.getTenantId(), homework.getAcademicYearId(), homework.getClassId(), homework.getSectionId());
        if (studentIds.isEmpty()) {
            return;
        }

        String subjectName = commonHelper.subjectName(homework.getSubjectId());
        String title = (!isEdit ? "New Homework: " : "Homework Updated: ") + homework.getTitle();
        studentNotificationService.notify(homework.getTenantId(), studentIds, Constant.NOTIFICATION_TYPE_HOMEWORK,
                homework.getHomeworkId(), title, subjectName != null ? subjectName : homework.getTitle());
    }

    @Override
    @Transactional
    public boolean deleteHomeWork(Integer homeworkId) {
        Homework homework = homeworkRepository.findById(homeworkId).orElseThrow(() -> new CustomException("homeworkId", "Item not found"));
        if (homework.getAttachmentUrl() != null && !homework.getAttachmentUrl().isEmpty()) {
            awsConfiguration.deleteFile(homework.getAttachmentUrl());
        }
        homework.setIsActive(false);
        homeworkRepository.save(homework);
        return true;
    }
}

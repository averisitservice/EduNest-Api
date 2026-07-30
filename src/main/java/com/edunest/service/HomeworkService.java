package com.edunest.service;

import com.edunest.dto.homework.HomeworkRequest;
import com.edunest.dto.homework.HomeworkResponse;

import java.util.List;

public interface HomeworkService {

    List<HomeworkResponse> getHomeWorkList(Integer tenantId, Integer classId, Integer sectionId);

    boolean saveHomeWork(Integer tenantId, Integer loginTeacherId, HomeworkRequest request);

    boolean deleteHomeWork(Integer tenantId, Integer homeworkId);
}

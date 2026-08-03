package com.edunest.repository;

import com.edunest.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {

    List<Announcement> findByTenantIdAndAcademicYearIdAndIsActiveTrueOrderByPublishDateDescAnnouncementIdDesc(
            Integer tenantId, Integer academicYearId);

    List<Announcement> findByStatusAndPublishDateLessThanEqualAndIsActiveTrue(String status, LocalDate today);
}

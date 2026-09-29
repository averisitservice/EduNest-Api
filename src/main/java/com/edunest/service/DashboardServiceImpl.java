package com.edunest.service;

import com.edunest.constant.Constant;
import com.edunest.dto.dashboard.DashboardSummaryResponse;
import com.edunest.dto.dashboard.DashboardSummaryResponse.AttendanceToday;
import com.edunest.dto.dashboard.DashboardSummaryResponse.LatestAnnouncement;
import com.edunest.entity.AcademicYear;
import com.edunest.entity.Announcement;
import com.edunest.helper.CommonHelper;
import com.edunest.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    TeacherRepository teacherRepository;

    @Autowired
    ClassMasterRepository classMasterRepository;

    @Autowired
    AttendanceRepository attendanceRepository;

    @Autowired
    FeePaymentRepository feePaymentRepository;

    @Autowired
    AnnouncementRepository announcementRepository;

    @Autowired
    CommonHelper commonHelper;

    @Override
    public DashboardSummaryResponse getSummary(Integer tenantId) {
        AcademicYear currentYear = commonHelper.getCurrentYear(tenantId);

        DashboardSummaryResponse dashboardSummaryResponse = new DashboardSummaryResponse();
        dashboardSummaryResponse.setTotalStudents(studentRepository.countByTenantIdAndIsActiveTrue(tenantId));
        dashboardSummaryResponse.setTotalTeachers(teacherRepository.countByTenantIdAndIsActiveTrue(tenantId));
        dashboardSummaryResponse.setTotalClasses(classMasterRepository.countByTenantIdAndIsActiveTrue(tenantId));

        dashboardSummaryResponse.setAttendanceToday(buildAttendanceToday(tenantId, currentYear.getAcademicYearId()));
        dashboardSummaryResponse.setFeeCollectedThisMonth(feeCollectedThisMonth(tenantId, currentYear.getAcademicYearId()));
        dashboardSummaryResponse.setLatestAnnouncements(buildLatestAnnouncements(tenantId, currentYear.getAcademicYearId()));

        return dashboardSummaryResponse;
    }

    private AttendanceToday buildAttendanceToday(Integer tenantId, Integer yearId) {
        LocalDate today = LocalDate.now();
        long present = attendanceRepository
                .countByTenantIdAndAcademicYearIdAndAttendanceDateAndStatus(tenantId, yearId, today, Constant.PRESENT);
        long absent = attendanceRepository
                .countByTenantIdAndAcademicYearIdAndAttendanceDateAndStatus(tenantId, yearId, today, Constant.ABSENT);
        long leave = attendanceRepository
                .countByTenantIdAndAcademicYearIdAndAttendanceDateAndStatus(tenantId, yearId, today, Constant.LEAVE);
        long marked = attendanceRepository
                .countByTenantIdAndAcademicYearIdAndAttendanceDate(tenantId, yearId, today);

        double percent = marked > 0 ? Math.round((present * 10000.0 / marked)) / 100.0 : 0.0;

        return new AttendanceToday(today, present, absent, leave, marked, percent);
    }

    private BigDecimal feeCollectedThisMonth(Integer tenantId, Integer yearId) {
        LocalDate today = LocalDate.now();
        LocalDate first = today.withDayOfMonth(1);
        LocalDate last = today.withDayOfMonth(today.lengthOfMonth());
        BigDecimal sum = feePaymentRepository.sumAmountBetween(tenantId, yearId, first, last);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    private List<LatestAnnouncement> buildLatestAnnouncements(Integer tenantId, Integer yearId) {
        List<Announcement> announcements = announcementRepository
                .findByTenantIdAndAcademicYearIdAndIsActiveTrueOrderByPublishDateDescAnnouncementIdDesc(tenantId, yearId);

        List<LatestAnnouncement> result = new ArrayList<>();
        for (Announcement announcement : announcements) {
            if (result.size() >= 5) break;
            result.add(new LatestAnnouncement(announcement.getAnnouncementId(), announcement.getTitle(), announcement.getAudience(), announcement.getPublishDate()));
        }
        return result;
    }
}

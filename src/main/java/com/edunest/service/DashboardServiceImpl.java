package com.edunest.service;

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
        Integer yearId = currentYear.getAcademicYearId();

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setTotalStudents(studentRepository.countByTenantIdAndIsActiveTrue(tenantId));
        response.setTotalTeachers(teacherRepository.countByTenantIdAndIsActiveTrue(tenantId));
        response.setTotalClasses(classMasterRepository.countByTenantIdAndIsActiveTrue(tenantId));

        response.setAttendanceToday(buildAttendanceToday(tenantId, yearId));
        response.setFeeCollectedThisMonth(feeCollectedThisMonth(tenantId, yearId));
        response.setLatestAnnouncements(buildLatestAnnouncements(tenantId, yearId));

        return response;
    }

    private AttendanceToday buildAttendanceToday(Integer tenantId, Integer yearId) {
        LocalDate today = LocalDate.now();
        long present = attendanceRepository
                .countByTenantIdAndAcademicYearIdAndAttendanceDateAndStatus(tenantId, yearId, today, "P");
        long absent = attendanceRepository
                .countByTenantIdAndAcademicYearIdAndAttendanceDateAndStatus(tenantId, yearId, today, "A");
        long late = attendanceRepository
                .countByTenantIdAndAcademicYearIdAndAttendanceDateAndStatus(tenantId, yearId, today, "L");
        long marked = attendanceRepository
                .countByTenantIdAndAcademicYearIdAndAttendanceDate(tenantId, yearId, today);

        double percent = marked > 0 ? Math.round(((present + late) * 10000.0 / marked)) / 100.0 : 0.0;

        return new AttendanceToday(today, present, absent, late, marked, percent);
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
        for (Announcement a : announcements) {
            if (result.size() >= 5) break;
            result.add(new LatestAnnouncement(a.getAnnouncementId(), a.getTitle(), a.getAudience(), a.getPublishDate()));
        }
        return result;
    }
}

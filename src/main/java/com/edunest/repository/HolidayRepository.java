package com.edunest.repository;

import com.edunest.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Integer> {

    List<Holiday> findByTenantIdAndAcademicYearIdAndIsActiveTrueOrderByStartDateAsc(Integer tenantId, Integer academicYearId);

    List<Holiday> findByTenantIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndIsActiveTrue(Integer tenantId, LocalDate date1, LocalDate date2);
}

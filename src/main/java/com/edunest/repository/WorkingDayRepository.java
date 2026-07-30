package com.edunest.repository;

import com.edunest.entity.WorkingDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkingDayRepository extends JpaRepository<WorkingDay, Integer> {
    List<WorkingDay> findByTenantIdAndIsActiveTrueOrderByDayOrder(Integer tenantId);

    WorkingDay findByTenantIdAndDayName(Integer tenantId, String dayName);
}
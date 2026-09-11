package com.edunest.repository;

import com.edunest.entity.TenantFeeSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantFeeSettingRepository extends JpaRepository<TenantFeeSetting, Integer> {

    Optional<TenantFeeSetting> findByTenantIdAndAcademicYearId(Integer tenantId, Integer academicYearId);
}

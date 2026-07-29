package com.edunest.repository;

import com.edunest.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Integer> {
    AcademicYear findByTenantIdAndIsCurrentTrue(Integer tenantId);
}
package com.edunest.repository;

import com.edunest.entity.ClassSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassSectionRepository extends JpaRepository<ClassSection, Integer> {
    List<ClassSection> findByClassIdAndTenantId(Integer classId, Integer tenantId);
    
    List<ClassSection> findByTenantIdAndClassIdAndIsActiveTrue(Integer tenantId, Integer classId);
}

package com.edunest.repository;

import com.edunest.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {


    @Query(value = "SELECT DISTINCT s FROM Student s "
            + "LEFT JOIN StudentClass sc ON sc.studentId = s.studentId AND sc.tenantId = :tenantId "
            + "WHERE s.tenantId = :tenantId AND s.isActive = true "
            + "AND (:classId IS NULL OR sc.classId = :classId) "
            + "AND (:sectionId IS NULL OR sc.sectionId = :sectionId) "
            + "AND (:search = '' "
            + "  OR LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE CONCAT('%', :search, '%') "
            + "  OR LOWER(s.admissionNo) LIKE CONCAT('%', :search, '%') "
            + "  OR LOWER(s.email) LIKE CONCAT('%', :search, '%') "
            + "  OR LOWER(s.mobileNo) LIKE CONCAT('%', :search, '%') "
            + "  OR LOWER(s.fatherName) LIKE CONCAT('%', :search, '%') "
            + "  OR LOWER(s.parentMobile) LIKE CONCAT('%', :search, '%') "
            + "  OR LOWER(sc.rollNo) LIKE CONCAT('%', :search, '%'))",
            countQuery = "SELECT COUNT(DISTINCT s) FROM Student s "
                    + "LEFT JOIN StudentClass sc ON sc.studentId = s.studentId AND sc.tenantId = :tenantId "
                    + "WHERE s.tenantId = :tenantId AND s.isActive = true "
                    + "AND (:classId IS NULL OR sc.classId = :classId) "
                    + "AND (:sectionId IS NULL OR sc.sectionId = :sectionId) "
                    + "AND (:search = '' "
                    + "  OR LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE CONCAT('%', :search, '%') "
                    + "  OR LOWER(s.admissionNo) LIKE CONCAT('%', :search, '%') "
                    + "  OR LOWER(s.email) LIKE CONCAT('%', :search, '%') "
                    + "  OR LOWER(s.mobileNo) LIKE CONCAT('%', :search, '%') "
                    + "  OR LOWER(s.fatherName) LIKE CONCAT('%', :search, '%') "
                    + "  OR LOWER(s.parentMobile) LIKE CONCAT('%', :search, '%') "
                    + "  OR LOWER(sc.rollNo) LIKE CONCAT('%', :search, '%'))")
    Page<Student> searchStudents(
            @Param("tenantId") Integer tenantId, @Param("search") String search,
            @Param("classId") Integer classId, @Param("sectionId") Integer sectionId, Pageable pageable);

    Optional<Student> findByUsernameIgnoreCase(String username);

    List<Student> findByEmailIgnoreCaseAndIsActiveTrue(String email);

    long countByTenantIdAndIsActiveTrue(Integer tenantId);

    @Query(value = """
            SELECT admission_no FROM auth.student
            WHERE tenant_id = :tenantId
            AND admission_no LIKE CONCAT(:year, '-%')
            ORDER BY admission_no DESC
            LIMIT 1
            """, nativeQuery = true)
    String findLastAdmissionNo(@Param("tenantId") Integer tenantId,
                               @Param("year") String year);

    @Query("SELECT s.studentId FROM Student s WHERE s.tenantId = :tenantId AND s.isActive = true")
    List<Integer> findActiveStudentIds(@Param("tenantId") Integer tenantId);
}

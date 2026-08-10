package com.edunest.repository;

import com.edunest.entity.StudentDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentDeviceTokenRepository extends JpaRepository<StudentDeviceToken, Integer> {

    Optional<StudentDeviceToken> findByFcmToken(String fcmToken);

    @Query("select distinct s.fcmToken from StudentDeviceToken s where s.tenantId = :tenantId and s.studentId in :studentIds")
    List<String> findDistinctFcmTokenByTenantIdAndStudentIdIn(@Param("tenantId") Integer tenantId, @Param("studentIds") List<Integer> studentIds);

    void deleteByFcmToken(String fcmToken);

    void deleteByTenantIdAndStudentIdAndFcmToken(Integer tenantId, Integer studentId, String fcmToken);
}

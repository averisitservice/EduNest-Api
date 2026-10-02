package com.edunest.repository;

import com.edunest.entity.StudentDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentDeviceTokenRepository extends JpaRepository<StudentDeviceToken, Integer> {

    Optional<StudentDeviceToken> findByFcmToken(String fcmToken);

    List<String> findDistinctFcmTokenByTenantIdAndStudentIdIn(Integer tenantId, List<Integer> studentIds);

    void deleteByFcmToken(String fcmToken);

    void deleteByTenantIdAndStudentIdAndFcmToken(Integer tenantId, Integer studentId, String fcmToken);
}

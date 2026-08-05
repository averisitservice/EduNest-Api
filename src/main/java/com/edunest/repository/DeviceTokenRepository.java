package com.edunest.repository;

import com.edunest.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Integer> {

    Optional<DeviceToken> findByFcmToken(String fcmToken);

    List<DeviceToken> findByTenantIdAndStudentId(Integer tenantId, Integer studentId);

    void deleteByFcmToken(String fcmToken);
}

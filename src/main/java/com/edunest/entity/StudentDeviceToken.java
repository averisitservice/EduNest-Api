package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_device_token", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentDeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_device_token_id")
    private Integer studentDeviceTokenId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "fcm_token", nullable = false, unique = true, length = 255)
    private String fcmToken;

    @Column(name = "device_id", length = 150)
    private String deviceId;

    @Column(name = "platform", length = 20)
    private String platform;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}

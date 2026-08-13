package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_notification", schema = "school")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_notification_id")
    private Integer studentNotificationId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @Column(name = "reference_id")
    private Integer referenceId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "body", length = 2000)
    private String body;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}

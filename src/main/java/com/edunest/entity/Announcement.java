package com.edunest.entity;

import com.edunest.constant.Constant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcement", schema = "school")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "announcement_id")
    private Integer announcementId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "academic_year_id", nullable = false)
    private Integer academicYearId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "message", nullable = false, length = 2000)
    private String message;

    @Column(name = "audience", nullable = false, length = 20)
    private String audience;

    // Comma-separated class IDs, e.g. "1,2,3" (empty/null means whole school)
    @Column(name = "class_ids", length = 500)
    private String classIds;

    @Column(name = "publish_date", nullable = false)
    private LocalDate publishDate;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = Constant.ANNOUNCEMENT_STATUS_PUBLISHED;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "created_by", updatable = false)
    private Integer createdBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "updated_by")
    private Integer updatedBy;
}

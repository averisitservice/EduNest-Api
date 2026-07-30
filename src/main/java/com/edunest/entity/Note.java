package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "note", schema = "school")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_id")
    private Integer noteId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "academic_year_id", nullable = false)
    private Integer academicYearId;

    @Column(name = "class_id", nullable = false)
    private Integer classId;

    @Column(name = "section_id")
    private Integer sectionId;

    @Column(name = "subject_id")
    private Integer subjectId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "is_active")
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

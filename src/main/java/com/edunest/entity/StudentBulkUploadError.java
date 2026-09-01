package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_bulk_upload_error", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentBulkUploadError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "error_id")
    private Integer errorId;

    @Column(name = "upload_id", nullable = false)
    private Integer uploadId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "excel_row_number", nullable = false)
    private Integer excelRowNumber;

    @Column(name = "student_identifier", length = 150)
    private String studentIdentifier;

    @Column(name = "error_type", nullable = false, length = 50)
    private String errorType;

    @Column(name = "error_reason", nullable = false, length = 500)
    private String errorReason;

    @Column(name = "row_data", columnDefinition = "TEXT")
    private String rowData;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
}

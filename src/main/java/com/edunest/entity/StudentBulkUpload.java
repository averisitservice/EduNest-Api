package com.edunest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_bulk_upload", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentBulkUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "upload_id")
    private Integer uploadId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "uploaded_by", nullable = false)
    private Integer uploadedBy;

    @Column(name = "total_rows")
    private Integer totalRows = 0;

    @Column(name = "success_rows")
    private Integer successRows = 0;

    @Column(name = "failed_rows")
    private Integer failedRows = 0;

    @Column(name = "inserted_rows")
    private Integer insertedRows = 0;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}

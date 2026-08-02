package com.edunest.repository;

import com.edunest.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Integer> {

    @Query("SELECT n FROM Note n WHERE n.tenantId = :tenantId AND n.academicYearId = :academicYearId "
            + "AND n.isActive = true AND n.classId = :classId "
            + "AND ((:sectionId IS NULL AND n.sectionId IS NULL) OR n.sectionId = :sectionId) "
            + "ORDER BY n.noteId DESC")
    List<Note> findList(
            @Param("tenantId") Integer tenantId, @Param("academicYearId") Integer academicYearId,
            @Param("classId") Integer classId, @Param("sectionId") Integer sectionId);

    @Query("SELECT n FROM Note n WHERE n.tenantId = :tenantId AND n.academicYearId = :academicYearId "
            + "AND n.isActive = true AND n.classId = :classId "
            + "AND (n.sectionId IS NULL OR n.sectionId = :sectionId) "
            + "AND (CAST(:fromDate AS timestamp) IS NULL OR n.createdDate >= CAST(:fromDate AS timestamp)) "
            + "AND (CAST(:toDate AS timestamp) IS NULL OR n.createdDate <= CAST(:toDate AS timestamp)) "
            + "ORDER BY n.noteId DESC")
    List<Note> findNoteForStudentInDateRange(
            @Param("tenantId") Integer tenantId, @Param("academicYearId") Integer academicYearId,
            @Param("classId") Integer classId, @Param("sectionId") Integer sectionId,
            @Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);
}

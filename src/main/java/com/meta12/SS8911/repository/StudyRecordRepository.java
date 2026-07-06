package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.entity.StudyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface StudyRecordRepository extends JpaRepository<StudyRecord, Long> {
    List<StudyRecord> findBySiteUserAndStudyDateBetween(SiteUser siteUser, LocalDateTime start, LocalDateTime end);
}
package com.businessos.modules.project.meeting;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    Page<Meeting> findByCompanyId(Long companyId, Pageable pageable);

    long countByCompanyId(Long companyId);

    @Query("""
        SELECT COUNT(m) FROM Meeting m
        WHERE m.company.id = :companyId
          AND m.startTime >= :start AND m.startTime < :end
          AND m.deleted = false
        """)
    long countByCompanyIdAndDay(
        @Param("companyId") Long companyId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);

    @Query("""
        SELECT m FROM Meeting m
        WHERE m.company.id = :companyId
          AND m.startTime >= :start AND m.startTime < :end
          AND m.deleted = false
        ORDER BY m.startTime ASC
        """)
    List<Meeting> findByCompanyIdAndDay(
        @Param("companyId") Long companyId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);
}

package com.businessos.modules.crm.lead;

import com.businessos.enums.LeadSource;
import com.businessos.enums.LeadStatus;
import com.businessos.enums.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    Optional<Lead> findByIdAndCompanyId(Long id, Long companyId);

    Page<Lead> findByCompanyId(Long companyId, Pageable pageable);

    Page<Lead> findByCompanyIdAndStatus(Long companyId, LeadStatus status, Pageable pageable);

    Page<Lead> findByCompanyIdAndAssignedToId(Long companyId, Long employeeId, Pageable pageable);


    @Query("SELECT l FROM Lead l WHERE l.company.id = :companyId AND " +
           "(LOWER(l.contactName) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\' OR " +
           "LOWER(l.email) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\' OR " +
           "LOWER(l.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\' OR " +
           "LOWER(l.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\') AND " +
           "l.deleted = false")
    Page<Lead> searchLeads(@Param("companyId") Long companyId, @Param("keyword") String keyword, Pageable pageable);

    Page<Lead> findByCompanyIdAndSource(Long companyId, LeadSource source, Pageable pageable);

    Page<Lead> findByCompanyIdAndPriority(Long companyId, Priority priority, Pageable pageable);


    @Query("SELECT l FROM Lead l WHERE l.company.id = :companyId " +
           "AND (:status IS NULL OR l.status = :status) " +
           "AND (:source IS NULL OR l.source = :source) " +
           "AND (:priority IS NULL OR l.priority = :priority) " +
           "AND (:assignedToId IS NULL OR l.assignedTo.id = :assignedToId) " +
           "AND l.deleted = false " +
           "ORDER BY l.createdAt DESC")
    Page<Lead> filterLeads(@Param("companyId") Long companyId,
                           @Param("status") LeadStatus status,
                           @Param("source") LeadSource source,
                           @Param("priority") Priority priority,
                           @Param("assignedToId") Long assignedToId,
                           Pageable pageable);

    long countByCompanyIdAndStatus(Long companyId, LeadStatus status);

    long countByCompanyIdAndStatusAndCreatedAtBetween(
            Long companyId, LeadStatus status, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(l) FROM Lead l WHERE l.company.id = :companyId AND " +
           "l.status NOT IN :closedStatuses AND l.deleted = false")
    long countActiveByCompanyId(@Param("companyId") Long companyId, 
                                @Param("closedStatuses") List<LeadStatus> closedStatuses);

    @Query("SELECT COUNT(l) FROM Lead l WHERE l.company.id = :companyId AND " +
           "l.assignedTo.id = :assignedToId AND l.status NOT IN :closedStatuses AND l.deleted = false")
    long countActiveByAssignee(@Param("companyId") Long companyId, 
                               @Param("assignedToId") Long assignedToId,
                               @Param("closedStatuses") List<LeadStatus> closedStatuses);


    @Query("SELECT l FROM Lead l WHERE l.company.id = :companyId AND " +
           "l.expectedCloseDate BETWEEN :startDate AND :endDate AND " +
           "l.deleted = false")
    List<Lead> findLeadsCloseExpectedBetween(@Param("companyId") Long companyId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    @Query("SELECT l FROM Lead l WHERE l.company.id = :companyId AND " +
           "l.lastContactDate IS NULL AND l.status != 'WON' AND l.status != 'LOST' AND " +
           "l.deleted = false")
    Page<Lead> findNeverContactedLeads(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT l FROM Lead l WHERE l.company.id = :companyId AND " +
           "l.lastContactDate < :beforeDate AND l.status NOT IN :closedStatuses AND " +
           "l.deleted = false")
    Page<Lead> findStalLeads(@Param("companyId") Long companyId,
                             @Param("beforeDate") LocalDateTime beforeDate,
                             @Param("closedStatuses") List<LeadStatus> closedStatuses,
                             Pageable pageable);

    @Query("SELECT l FROM Lead l WHERE l.company.id = :companyId AND " +
           "l.assignedTo IS NULL AND l.status NOT IN :closedStatuses AND " +
           "l.deleted = false")
    Page<Lead> findUnassignedLeads(@Param("companyId") Long companyId,
                                   @Param("closedStatuses") List<LeadStatus> closedStatuses,
                                   Pageable pageable);

    @Query("SELECT l FROM Lead l WHERE l.company.id = :companyId AND " +
           "l.priority = 'HIGH' AND l.status NOT IN :closedStatuses AND " +
           "l.deleted = false " +
           "ORDER BY l.expectedCloseDate ASC")
    Page<Lead> findHighPriorityOpenLeads(@Param("companyId") Long companyId,
                                         @Param("closedStatuses") List<LeadStatus> closedStatuses,
                                         Pageable pageable);

    boolean existsByEmailAndCompanyIdAndDeletedFalse(String email, Long companyId);

    boolean existsByPhoneAndCompanyIdAndDeletedFalse(String phone, Long companyId);
    long countByCompanyId(Long companyId);
}

package com.businessos.modules.support.ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    @EntityGraph(attributePaths = {"createdBy", "category", "assignedToAgent", "assignedToAgent.user"})
    Optional<SupportTicket> findByTicketNumber(String ticketNumber);

    @EntityGraph(attributePaths = {"createdBy", "category", "assignedToAgent", "assignedToAgent.user"})
    Page<SupportTicket> findByCompanyIdAndStatus(Long companyId, TicketStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"createdBy", "category", "assignedToAgent", "assignedToAgent.user"})
    Page<SupportTicket> findByCompanyId(Long companyId, Pageable pageable);

    @EntityGraph(attributePaths = {"createdBy", "category", "assignedToAgent", "assignedToAgent.user"})
    Page<SupportTicket> findByStatus(TicketStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"createdBy", "category", "assignedToAgent", "assignedToAgent.user"})
    Page<SupportTicket> findByAssignedToAgentId(Long agentId, Pageable pageable);

    @EntityGraph(attributePaths = {"createdBy", "category", "assignedToAgent", "assignedToAgent.user"})
    Page<SupportTicket> findByCreatedById(Long userId, Pageable pageable);

    @Query("SELECT t FROM SupportTicket t WHERE t.status = :status AND t.slaBreached = false AND t.resolutionDeadline < :now")
    List<SupportTicket> findSLABreachedTickets(@Param("status") TicketStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT t FROM SupportTicket t WHERE t.status IN ('OPEN', 'IN_PROGRESS') AND t.priority = 'CRITICAL'")
    List<SupportTicket> findOpenCriticalTickets();

    long countByStatusAndCompanyId(TicketStatus status, Long companyId);

    long countByAssignedToAgentId(Long agentId);

    Page<SupportTicket> findByCompanyIdAndTitleContainingIgnoreCase(Long companyId, String keyword, Pageable pageable);
}

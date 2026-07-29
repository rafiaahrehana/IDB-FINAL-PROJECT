package com.businessos.modules.support.ticket;

import com.businessos.modules.support.agent.SupportAgent;
import com.businessos.modules.support.agent.SupportAgentRepository;
import com.businessos.shared.audit.AuditLog;
import com.businessos.modules.support.audit.SupportAuditLogRepository;
import com.businessos.modules.support.category.SupportCategory;
import com.businessos.modules.support.category.SupportCategoryRepository;
import com.businessos.modules.support.sla.SLAPolicy;
import com.businessos.modules.support.sla.SLAPolicyRepository;
import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.auth.user.User;
import com.businessos.auth.user.UserRepository;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final SupportCategoryRepository categoryRepository;
    private final SupportAgentRepository agentRepository;
    private final SLAPolicyRepository slaPolicyRepository;
    private final SupportAuditLogRepository auditRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional
    public SupportTicketResponse create(SupportTicketRequest request) {
        Long companyId = securityUtil.getCurrentCompanyId();
        Long currentUserId = securityUtil.getCurrentUser().getId();

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        User createdBy = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SupportCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        String ticketNumber = generateTicketNumber();

        // Get SLA policy based on priority
        SLAPolicy slaPolicy = slaPolicyRepository.findByApplicablePriorityAndActiveTrue(request.getPriority())
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstResponseDeadline = null;
        LocalDateTime resolutionDeadline = null;

        if (slaPolicy != null) {
            firstResponseDeadline = now.plusHours(slaPolicy.getFirstResponseTimeHours());
            resolutionDeadline = now.plusHours(slaPolicy.getResolutionTimeHours());
        }

        SupportTicket ticket = SupportTicket.builder()
                .companyId(companyId)
                .ticketNumber(ticketNumber)
                .company(company)
                .createdBy(createdBy)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(category)
                .status(TicketStatus.NEW)
                .priority(request.getPriority())
                .source(request.getSource())
                .firstResponseDeadline(firstResponseDeadline)
                .resolutionDeadline(resolutionDeadline)
                .build();

        ticket = ticketRepository.save(ticket);

        // Log audit
        logAudit(companyId, currentUserId, "CREATE_TICKET", ticket.getId(), "SupportTicket",
                "Ticket created: " + ticketNumber, null);

        return SupportTicketMapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketResponse getById(Long id) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        return SupportTicketMapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketResponse getByTicketNumber(String number) {
        SupportTicket ticket = ticketRepository.findByTicketNumber(number)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        return SupportTicketMapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getAll(Pageable pageable) {
        // Platform support staff (SUPPORT_MANAGER/SUPER_ADMIN/SYSTEM_ADMIN) triage
        // tickets across every company - only a tenant caller (COMPANY_OWNER) gets
        // scoped to their own company. findAll() here was previously unscoped for
        // everyone, letting a company owner see every other tenant's tickets.
        User current = securityUtil.getCurrentUser();
        if (current != null && !current.isPlatformUser()) {
            // Fine-grained permission only applies to tenant users - platform staff
            // (SUPPORT_MANAGER etc.) have no CustomRole, so checkPermission() would
            // always deny them; their existing role-based @PreAuthorize already gates
            // this endpoint for that branch.
            authorizationService.checkPermission(PermissionCode.TICKET_VIEW);
            return ticketRepository.findByCompanyId(securityUtil.getCurrentCompanyId(), pageable)
                    .map(SupportTicketMapper::toResponse);
        }
        return ticketRepository.findAll(pageable)
                .map(SupportTicketMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getByCompany(Long companyId, Pageable pageable) {
        return ticketRepository.findByCompanyId(companyId, pageable)
                .map(SupportTicketMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getByStatus(TicketStatus status, Pageable pageable) {
        User current = securityUtil.getCurrentUser();
        if (current != null && !current.isPlatformUser()) {
            authorizationService.checkPermission(PermissionCode.TICKET_VIEW);
            return ticketRepository.findByCompanyIdAndStatus(securityUtil.getCurrentCompanyId(), status, pageable)
                    .map(SupportTicketMapper::toResponse);
        }
        return ticketRepository.findByStatus(status, pageable)
                .map(SupportTicketMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getAssignedToMe(Long agentId, Pageable pageable) {
        if (agentId == null) {
            return Page.empty(pageable);
        }
        return ticketRepository.findByAssignedToAgentId(agentId, pageable)
                .map(SupportTicketMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getMyTickets(Long userId, Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.TICKET_VIEW);
        Long targetUserId = userId != null ? userId : securityUtil.getCurrentUser().getId();
        return ticketRepository.findByCreatedById(targetUserId, pageable)
                .map(SupportTicketMapper::toResponse);
    }

    @Override
    @Transactional
    public void assignToAgent(Long ticketId, Long agentId) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        SupportAgent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));

        ticket.assignToAgent(agent);
        ticketRepository.save(ticket);

        logAudit(ticket.getCompanyId(), securityUtil.getCurrentUser().getId(), "ASSIGN", ticketId,
                "SupportTicket", "Assigned to " + agent.getUser().getFullName(), null);
    }

    @Override
    @Transactional
    public void reassignToAgent(Long ticketId, Long newAgentId, String reason) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        SupportAgent newAgent = agentRepository.findById(newAgentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));

        SupportAgent oldAgent = ticket.getAssignedToAgent();
        ticket.setAssignedToAgent(newAgent);
        ticket.setAssignedDate(LocalDateTime.now());
        ticketRepository.save(ticket);

        String oldAgentName = oldAgent != null ? oldAgent.getUser().getFullName() : "Unassigned";
        String description = String.format("Reassigned from %s to %s. Reason: %s",
                oldAgentName, newAgent.getUser().getFullName(), reason);

        logAudit(ticket.getCompanyId(), securityUtil.getCurrentUser().getId(), "REASSIGN", ticketId,
                "SupportTicket", description, null);
    }

    @Override
    @Transactional
    public void escalate(Long ticketId, String reason) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        ticket.setEscalationLevel(ticket.getEscalationLevel() + 1);
        ticket.setEscalatedDate(LocalDateTime.now());
        ticket.setEscalationReason(reason);
        ticketRepository.save(ticket);

        logAudit(ticket.getCompanyId(), securityUtil.getCurrentUser().getId(), "ESCALATE", ticketId,
                "SupportTicket", "Escalated to level " + ticket.getEscalationLevel() + ": " + reason, null);
    }

    @Override
    @Transactional
    public void recordFirstResponse(Long ticketId) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        ticket.recordFirstResponse();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticketRepository.save(ticket);

        logAudit(ticket.getCompanyId(), securityUtil.getCurrentUser().getId(), "FIRST_RESPONSE", ticketId,
                "SupportTicket", "First response recorded", null);
    }

    @Override
    @Transactional
    public void resolve(Long ticketId, String resolutionNotes) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        Long currentUserId = securityUtil.getCurrentUser().getId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ticket.resolve(resolutionNotes, user.getFullName());
        ticketRepository.save(ticket);

        logAudit(ticket.getCompanyId(), currentUserId, "RESOLVE", ticketId,
                "SupportTicket", "Ticket resolved", null);
    }

    @Override
    @Transactional
    public void close(Long ticketId) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        ticket.close();
        ticketRepository.save(ticket);

        logAudit(ticket.getCompanyId(), securityUtil.getCurrentUser().getId(), "CLOSE", ticketId,
                "SupportTicket", "Ticket closed", null);
    }

    @Override
    @Transactional
    public void reopen(Long ticketId, String reason) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        ticket.setStatus(TicketStatus.REOPENED);
        ticketRepository.save(ticket);

        logAudit(ticket.getCompanyId(), securityUtil.getCurrentUser().getId(), "REOPEN", ticketId,
                "SupportTicket", "Ticket reopened: " + reason, null);
    }

    @Override
    @Transactional
    public void recordSatisfaction(Long ticketId, int rating, String feedback) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        ticket.setSatisfactionRating(rating);
        ticket.setSatisfactionFeedback(feedback);
        ticketRepository.save(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getSLABreachedTickets() {
        Long companyId = securityUtil.getCurrentCompanyId();
        List<SupportTicket> tickets = ticketRepository.findSLABreachedTickets(TicketStatus.OPEN, LocalDateTime.now());
        if (companyId != null) {
            tickets = tickets.stream()
                    .filter(t -> companyId.equals(t.getCompanyId()))
                    .collect(Collectors.toList());
        }
        return tickets.stream()
                .map(SupportTicketMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getOpenCriticalTickets() {
        Long companyId = securityUtil.getCurrentCompanyId();
        List<SupportTicket> tickets = ticketRepository.findOpenCriticalTickets();
        if (companyId != null) {
            tickets = tickets.stream()
                    .filter(t -> companyId.equals(t.getCompanyId()))
                    .collect(Collectors.toList());
        }
        return tickets.stream()
                .map(SupportTicketMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SupportTicketResponse update(Long id, SupportTicketRequest request) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());

        if (request.getCategoryId() != null) {
            SupportCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            ticket.setCategory(category);
        }

        ticket = ticketRepository.save(ticket);
        return SupportTicketMapper.toResponse(ticket);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        ticket.softDelete();
        ticketRepository.save(ticket);
    }

    private String generateTicketNumber() {
        long count = ticketRepository.count() + 1;
        return String.format("TKT-%04d-%06d", LocalDate.now().getYear(), count);
    }

    private void logAudit(Long companyId, Long userId, String actionType, Long resourceId,
                          String resourceType, String description, String changes) {
        Company company = companyRepository.findById(companyId).orElse(null);
        AuditLog log = AuditLog.builder()
                .company(company)
                .performedBy(userRepository.findById(userId).orElse(null))
                .action(com.businessos.enums.AuditAction.valueOf(actionType))
                .entityId(resourceId)
                .entityType(com.businessos.enums.AuditEntityType.valueOf("SUPPORT_TICKET"))
                .oldValue(description)
                .newValue(changes)
                .build();
        auditRepository.save(log);
    }
}

package com.businessos.modules.crm.lead;

import com.businessos.modules.ai.enums.AiFeature;
import com.businessos.modules.ai.prompt.CrmSummaryPromptBuilder;
import com.businessos.modules.ai.service.AiService;
import com.businessos.auth.role.enums.Role;
import com.businessos.modules.crm.client.Client;
import com.businessos.modules.servicedesk.companyservice.CompanyServiceRepository;
import com.businessos.modules.company.Company;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.auth.user.User;
import com.businessos.auth.user.UserRepository;
import com.businessos.enums.*;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.modules.crm.client.ClientRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.notification.CreateNotificationRequest;
import com.businessos.shared.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LeadServiceImpl implements LeadService {

    private final EmployeeRepository employeeRepository;
    private final CompanyServiceRepository companyServiceRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtil securityUtil;
    private final AiService aiService;
    private final LeadRepository leadRepository;
    private final com.businessos.modules.crm.activity.CrmActivityRepository leadActivityRepository;
    private final LeadMapper leadMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public LeadResponse createLead(LeadRequest request) {
        validateLeadRequest(request);
        Long companyId = requireCompanyId();

        // Check for duplicate email
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (leadRepository.existsByEmailAndCompanyIdAndDeletedFalse(request.getEmail(), companyId)) {
                throw new BadRequestException("A lead with this email already exists in your company");
            }
        }

        Lead lead = Lead.builder()
                .contactName(request.getContactName())
                .companyName(request.getCompanyName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .industry(request.getIndustry())
                .jobTitle(request.getJobTitle())
                .notes(request.getNotes())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : LeadStatus.NEW)
                .source(request.getSource() != null ? request.getSource() : LeadSource.OTHER)
                .priority(request.getPriority() != null ? request.getPriority() : Priority.NORMAL)
                .estimatedValue(request.getEstimatedValue())
                .expectedCloseDate(request.getExpectedCloseDate())
                .company(companyRef(companyId))
                .build();

        if (request.getAssignedToId() != null) {
            Employee assignee = findEmployee(request.getAssignedToId(), companyId);
            lead.setAssignedTo(assignee);
        }

        if (request.getInterestedServiceId() != null) {
            lead.setInterestedService(
                    companyServiceRepository.findByIdAndCompanyId(request.getInterestedServiceId(), companyId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Service not found: " + request.getInterestedServiceId())));
        }

        Lead saved = leadRepository.save(lead);
        notifyAssignee(saved);
        return leadMapper.toLeadResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LeadResponse getLeadById(Long id) {
        return leadMapper.toLeadResponse(findLeadInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponse> listLeads(LeadStatus status, Pageable pageable) {
        Long companyId = requireCompanyId();
        return (status != null
                ? leadRepository.findByCompanyIdAndStatus(companyId, status, pageable)
                : leadRepository.findByCompanyId(companyId, pageable))
                .map(leadMapper::toLeadResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponse> listMyLeads(Pageable pageable) {
        Long companyId = requireCompanyId();
        Employee emp = employeeRepository.findByUserId(securityUtil.getCurrentUser().getId())
                .orElseThrow(() -> new BadRequestException("Employee profile not found"));
        return leadRepository.findByCompanyIdAndAssignedToId(companyId, emp.getId(), pageable)
                .map(leadMapper::toLeadResponse);
    }

    @Override
    @Transactional
    public LeadResponse updateLead(Long id, LeadRequest request) {
        validateLeadRequest(request);
        Long companyId = requireCompanyId();
        Lead lead = findLeadInTenant(id);

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(lead.getEmail())) {
            if (leadRepository.existsByEmailAndCompanyIdAndDeletedFalse(request.getEmail(), companyId)) {
                throw new BadRequestException("A lead with this email already exists in your company");
            }
        }

        // Prevent editing closed leads
        if (lead.getStatus() == LeadStatus.WON || lead.getStatus() == LeadStatus.LOST) {
            throw new BadRequestException("Cannot edit a closed lead");
        }

        // Update fields if provided
        if (request.getContactName() != null)
            lead.setContactName(request.getContactName());
        if (request.getCompanyName() != null)
            lead.setCompanyName(request.getCompanyName());
        if (request.getEmail() != null)
            lead.setEmail(request.getEmail());
        if (request.getPhone() != null)
            lead.setPhone(request.getPhone());
        if (request.getIndustry() != null)
            lead.setIndustry(request.getIndustry());
        if (request.getJobTitle() != null)
            lead.setJobTitle(request.getJobTitle());
        if (request.getNotes() != null)
            lead.setNotes(request.getNotes());
        if (request.getDescription() != null)
            lead.setDescription(request.getDescription());
        if (request.getStatus() != null)
            lead.setStatus(request.getStatus());
        if (request.getSource() != null)
            lead.setSource(request.getSource());
        if (request.getPriority() != null)
            lead.setPriority(request.getPriority());
        if (request.getEstimatedValue() != null)
            lead.setEstimatedValue(request.getEstimatedValue());
        if (request.getExpectedCloseDate() != null)
            lead.setExpectedCloseDate(request.getExpectedCloseDate());

        boolean reassigned = false;
        if (request.getAssignedToId() != null) {
            Employee assignee = findEmployee(request.getAssignedToId(), companyId);
            reassigned = lead.getAssignedTo() == null || !lead.getAssignedTo().getId().equals(assignee.getId());
            lead.setAssignedTo(assignee);
        }

        Lead saved = leadRepository.save(lead);
        if (reassigned) notifyAssignee(saved);
        return leadMapper.toLeadResponse(saved);
    }

    @Override
    @Transactional
    public void deleteLead(Long id) {
        Lead lead = findLeadInTenant(id);
        lead.setDeleted(true);
        lead.setDeletedAt(LocalDateTime.now());
        leadRepository.save(lead);

        // Also soft delete associated activities
        lead.getActivities().forEach(activity -> {
            activity.setDeleted(true);
            activity.setDeletedAt(LocalDateTime.now());
        });
    }

    // ==================== Search & Filter ====================

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponse> searchLeads(String keyword, Pageable pageable) {
        Long companyId = requireCompanyId();
        return leadRepository.searchLeads(companyId, keyword, pageable)
                .map(leadMapper::toLeadResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponse> filterLeads(LeadFilterRequest filter, Pageable pageable) {
        Long companyId = requireCompanyId();

        // Handle custom sorting
        if (filter.getSortBy() != null && filter.getSortDirection() != null) {
            Sort.Direction direction = Sort.Direction.fromString(filter.getSortDirection());
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(direction, filter.getSortBy()));
        }

        // If keyword is provided, use search instead
        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            return searchLeads(filter.getKeyword(), pageable);
        }

        // Handle special filters
        if (Boolean.TRUE.equals(filter.getIsUnassigned())) {
            return findUnassignedLeads(companyId, pageable);
        }

        if (Boolean.TRUE.equals(filter.getIsHighPriority())) {
            return findHighPriorityOpenLeads(companyId, pageable);
        }

        // Standard filter
        return leadRepository.filterLeads(
                companyId,
                filter.getStatus(),
                filter.getSource(),
                filter.getPriority(),
                filter.getAssignedToId(),
                pageable).map(leadMapper::toLeadResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponse> findLeadsBySource(LeadSource source, Pageable pageable) {
        Long companyId = requireCompanyId();
        return leadRepository.findByCompanyIdAndSource(companyId, source, pageable)
                .map(leadMapper::toLeadResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponse> findLeadsByPriority(Priority priority, Pageable pageable) {
        Long companyId = requireCompanyId();
        return leadRepository.findByCompanyIdAndPriority(companyId, priority, pageable)
                .map(leadMapper::toLeadResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponse> findUnassignedLeads(Long companyId, Pageable pageable) {
        if (companyId == null) {
            companyId = requireCompanyId();
        }
        List<LeadStatus> closedStatuses = List.of(LeadStatus.WON, LeadStatus.LOST);
        return leadRepository.findUnassignedLeads(companyId, closedStatuses, pageable)
                .map(leadMapper::toLeadResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponse> findHighPriorityOpenLeads(Long companyId, Pageable pageable) {
        if (companyId == null) {
            companyId = requireCompanyId();
        }
        List<LeadStatus> closedStatuses = List.of(LeadStatus.WON, LeadStatus.LOST);
        return leadRepository.findHighPriorityOpenLeads(companyId, closedStatuses, pageable)
                .map(leadMapper::toLeadResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponse> findNeverContactedLeads(Pageable pageable) {
        Long companyId = requireCompanyId();
        return leadRepository.findNeverContactedLeads(companyId, pageable)
                .map(leadMapper::toLeadResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponse> findStalLeads(Pageable pageable) {
        Long companyId = requireCompanyId();
        LocalDateTime stalDateTime = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        List<LeadStatus> closedStatuses = List.of(LeadStatus.WON, LeadStatus.LOST);
        return leadRepository.findStalLeads(companyId, stalDateTime, closedStatuses, pageable)
                .map(leadMapper::toLeadResponse);
    }

    // ==================== Lead Conversion ====================

    @Override
    @Transactional
    public LeadResponse convertLead(Long id) {
        Long companyId = requireCompanyId();
        Lead lead = findLeadInTenant(id);

        if (lead.isConverted() || lead.getStatus() == LeadStatus.WON) {
            throw new BadRequestException("Lead is already converted");
        }

        if (lead.getEmail() == null || lead.getEmail().isBlank()) {
            throw new BadRequestException("Lead must have an email address to be converted");
        }

        String normalizedEmail = lead.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("A platformuser with this email already exists");
        }

        // Create portal platformuser for the lead
        User user = User.builder()
                .firstName(lead.getContactName().split(" ")[0])
                .lastName(lead.getContactName().contains(" ")
                        ? lead.getContactName().substring(lead.getContactName().indexOf(' ') + 1)
                        : "")
                .email(normalizedEmail)
                .phone(lead.getPhone())
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.CLIENT)
                .active(true)
                .emailVerified(false)
                .build();
        userRepository.save(user);

        // Create client
        Client client = Client.builder()
                .clientCompanyName(lead.getCompanyName() != null ? lead.getCompanyName() : lead.getContactName())
                .user(user)
                .company(companyRef(companyId))
                .status(ClientStatus.ACTIVE)
                .industry(lead.getIndustry())
                .onboardedAt(LocalDate.now())
                .build();
        clientRepository.save(client);

        // Update lead
        lead.setConverted(true);
        lead.setConvertedAt(LocalDateTime.now());
        lead.setConvertedClient(client);
        lead.setStatus(LeadStatus.WON);

        return leadMapper.toLeadResponse(leadRepository.save(lead));
    }

    // ==================== Activity Management ====================

    @Override
    @Transactional
    public com.businessos.modules.crm.activity.CrmActivityResponse addActivity(Long leadId, com.businessos.modules.crm.activity.CrmActivityRequest request) {
        Long companyId = requireCompanyId();
        Lead lead = findLeadInTenant(leadId);

        com.businessos.modules.crm.activity.CrmActivity activity = com.businessos.modules.crm.activity.CrmActivity.builder()
                .lead(lead)
                .performedBy(securityUtil.getCurrentUser())
                .type(request.getType() != null ? request.getType() : com.businessos.modules.crm.activity.CrmActivityType.NOTE)
                .subject(request.getSubject())
                .description(request.getDescription())
                .activityDate(request.getActivityDate() != null ? request.getActivityDate() : LocalDateTime.now())
                .company(companyRef(companyId))
                .build();

        com.businessos.modules.crm.activity.CrmActivity saved = leadActivityRepository.save(activity);

        // Update lead's last activity
        lead.setLastActivityAt(LocalDateTime.now());
        if (request.getType() == com.businessos.modules.crm.activity.CrmActivityType.CALL ||
                request.getType() == com.businessos.modules.crm.activity.CrmActivityType.MEETING ||
                request.getType() == com.businessos.modules.crm.activity.CrmActivityType.EMAIL) {
            lead.setLastContactDate(LocalDateTime.now().toLocalDate());
        }
        leadRepository.save(lead);

        return leadMapper.toActivityResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<com.businessos.modules.crm.activity.CrmActivityResponse> getActivities(Long leadId, Pageable pageable) {
        findLeadInTenant(leadId);
        return leadActivityRepository.findByLeadId(leadId, pageable)
                .map(leadMapper::toActivityResponse);
    }

    @Override
    @Transactional
    public void deleteActivity(Long leadId, Long activityId) {
        findLeadInTenant(leadId);
        com.businessos.modules.crm.activity.CrmActivity activity = leadActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        if (!activity.getLead().getId().equals(leadId)) {
            throw new BadRequestException("Activity does not belong to this lead");
        }

        activity.setDeleted(true);
        activity.setDeletedAt(LocalDateTime.now());
        leadActivityRepository.save(activity);
    }

    // ==================== Dashboard & Reporting ====================

    @Override
    @Transactional(readOnly = true)
    public long countLeadsByStatus(LeadStatus status) {
        Long companyId = requireCompanyId();
        return leadRepository.countByCompanyIdAndStatus(companyId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveLeads() {
        Long companyId = requireCompanyId();
        List<LeadStatus> closedStatuses = List.of(LeadStatus.WON, LeadStatus.LOST);
        return leadRepository.countActiveByCompanyId(companyId, closedStatuses);
    }

    @Override
    @Transactional(readOnly = true)
    public long countMyActiveLeads() {
        Long companyId = requireCompanyId();
        Employee emp = employeeRepository.findByUserId(securityUtil.getCurrentUser().getId())
                .orElseThrow(() -> new BadRequestException("Employee profile not found"));
        List<LeadStatus> closedStatuses = List.of(LeadStatus.WON, LeadStatus.LOST);
        return leadRepository.countActiveByAssignee(companyId, emp.getId(), closedStatuses);
    }

    @Override
    @Transactional(readOnly = true)
    public LeadResponse summariseLead(Long id) {
        Lead lead = findLeadInTenant(id);

        // Use AI to summarize if available
        try {
            String prompt = new CrmSummaryPromptBuilder()
                    .setContactName(lead.getContactName())
                    .setCurrentStatus(lead.getStatus().name())
                    .setActivityHistory("Activities count: " + lead.getActivities().size())
                    .build();
            String summary = aiService.generateFromPrompt(AiFeature.CRM_LEAD_SUMMARY, prompt);
            // Store summary (optional: add to lead description)
        } catch (Exception e) {
            log.error("Failed to generate AI summary for lead with ID {}: {}", id, e.getMessage(), e);
        }

        return leadMapper.toLeadResponse(lead);
    }

    private void notifyAssignee(Lead lead) {
        Employee assignee = lead.getAssignedTo();
        if (assignee == null || assignee.getUser() == null) return;
        notificationService.send(CreateNotificationRequest.of(
                NotificationType.GENERAL,
                "Lead Assigned",
                "Lead \"" + lead.getContactName() + "\" has been assigned to you.",
                "/crm/leads",
                assignee.getUser().getId(),
                lead.getCompany().getId()
        ));
    }

    private Lead findLeadInTenant(Long id) {
        Long companyId = requireCompanyId();
        return leadRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));
    }

    private Long requireCompanyId() {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null) {
            throw new BadRequestException("No company context found");
        }
        return companyId;
    }

    private Employee findEmployee(Long employeeId, Long companyId) {
        return employeeRepository.findByIdAndCompanyId(employeeId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
    }

    private Company companyRef(Long companyId) {
        Company company = new Company();
        company.setId(companyId);
        return company;
    }

    private void validateLeadRequest(LeadRequest request) {
        if (request.getContactName() == null || request.getContactName().isBlank()) {
            throw new BadRequestException("Contact name is required");
        }
        if (request.getContactName().length() < 2) {
            throw new BadRequestException("Contact name must be at least 2 characters");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank() && !isValidEmail(request.getEmail())) {
            throw new BadRequestException("Email format is invalid");
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}

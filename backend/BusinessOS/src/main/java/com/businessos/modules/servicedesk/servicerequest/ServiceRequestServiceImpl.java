package com.businessos.modules.servicedesk.servicerequest;

import com.businessos.core.automation.AutomationEventPublisher;
import com.businessos.enums.*;
import com.businessos.core.subscription.SubscriptionStatus;
import com.businessos.modules.servicedesk.companyservice.CompanyService;
import com.businessos.modules.servicedesk.companyservice.PackageSubscription;
import com.businessos.modules.servicedesk.companyservice.PackageSubscriptionRepository;
import com.businessos.modules.servicedesk.companyservice.ServicePackageService;
import com.businessos.modules.servicedesk.requestcomment.AddCommentRequest;
import com.businessos.modules.servicedesk.requeststatus.ChangeRequestStatusRequest;
import com.businessos.shared.exception.ForbiddenException;
import com.businessos.shared.notification.CreateNotificationRequest;
import com.businessos.modules.servicedesk.requestcomment.RequestCommentResponse;
import com.businessos.modules.servicedesk.requeststatus.RequestStatusHistoryResponse;
import com.businessos.modules.crm.client.Client;
import com.businessos.modules.company.Company;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.servicedesk.requestcomment.RequestComment;
import com.businessos.modules.servicedesk.requestcomment.RequestStatusHistory;
import com.businessos.auth.user.User;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.modules.crm.client.ClientRepository;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.modules.servicedesk.companyservice.CompanyServiceRepository;
import com.businessos.modules.servicedesk.requestcomment.RequestCommentRepository;
import com.businessos.modules.servicedesk.requeststatus.RequestStatusHistoryRepository;
import com.businessos.modules.servicedesk.task.TaskRepository;
import com.businessos.modules.servicedesk.workflow.stage.WorkflowStageRepository;
import com.businessos.modules.servicedesk.workflow.stage.WorkflowStage;
import com.businessos.modules.servicedesk.approval.StageApproval;
import com.businessos.modules.servicedesk.approval.StageApprovalRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.notification.NotificationService;
import com.businessos.shared.email.EmailBranding;
import com.businessos.shared.email.EmailService;
import com.businessos.modules.finance.invoice.ClientInvoiceService;
import com.businessos.modules.finance.invoice.ClientInvoiceRequest;
import com.businessos.modules.finance.invoice.ClientInvoiceResponse;
import com.businessos.modules.finance.invoice.ClientInvoiceItemRequest;
import com.businessos.modules.company.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRequestServiceImpl implements ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final TaskRepository taskRepository;
    private final RequestCommentRepository commentRepository;
    private final RequestStatusHistoryRepository historyRepository;
    private final CompanyServiceRepository companyServiceRepository;
    private final PackageSubscriptionRepository subscriptionRepository;
    private final ServicePackageService packageService;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkflowStageRepository workflowStageRepository;
    private final StageApprovalRepository stageApprovalRepository;
    private final NotificationService notificationService;
    private final SecurityUtil securityUtil;
    private final EmailService emailService;
    private final EmailBranding emailBranding;
    private final CompanyRepository companyRepository;
    private final AutomationEventPublisher automationEventPublisher;
    private final ClientInvoiceService invoiceService;

    @Override
    @Transactional
    public ServiceRequestResponse create(CreateServiceRequestRequest request) {
        Long companyId = requireCompanyId();
        User currentUser = securityUtil.getCurrentUser();

        Client client = clientRepository.findByUserId(currentUser.getId())
            .orElseThrow(() -> new BadRequestException(
                "Only clients can submit service requests"));

        CompanyService service = companyServiceRepository
            .findByIdAndCompanyId(request.getHubServiceId(), companyId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Service not found: " + request.getHubServiceId()));

        if (!service.isActive()) {
            throw new BadRequestException("This service is currently unavailable");
        }

        PackageSubscription subscription = null;
        BigDecimal agreedPrice;

        if (request.getSubscriptionId() != null) {
            // Validate the subscription belongs to this client and tenant
            subscription = subscriptionRepository
                .findByIdAndCompanyId(request.getSubscriptionId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Subscription not found: " + request.getSubscriptionId()));

            if (!subscription.getClient().getId().equals(client.getId())) {
                throw new BadRequestException(
                    "This subscription does not belong to you");
            }
            if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
                throw new BadRequestException(
                    "Subscription is not active. Status: " + subscription.getStatus());
            }
            // Verify the requested service is included in the package
            if (!subscription.getServicePackage().includesService(service.getId())) {
                throw new BadRequestException(
                    "Service '" + service.getName() +
                    "' is not included in your subscription package");
            }
            // Consume quota — throws if exhausted
            packageService.consumeQuota(subscription.getId());

            // Included in package — no extra charge
            agreedPrice = BigDecimal.ZERO;

        } else {
            agreedPrice = request.getAgreedPrice() != null
                ? request.getAgreedPrice()
                : service.getPrice();
        }

        ServiceRequest sr = ServiceRequest.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .status(ServiceRequestStatus.PENDING)
            .priority(request.getPriority() != null
                ? request.getPriority() : service.getDefaultPriority())
            .agreedPrice(agreedPrice)
            .slaDeadline(request.getSlaDeadline())
            .company(companyRef(companyId))
            .client(client)
            .companyService(service)
            .subscription(subscription)
            .build();

        serviceRequestRepository.save(sr);
        recordStatusChange(sr, null, ServiceRequestStatus.PENDING,
            "Request submitted", currentUser, companyId);

        if (client.getUser() != null) {
            try {
                Company fullCompany = companyRepository.findById(companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
                EmailBranding.Data branding = emailBranding.from(fullCompany);
                emailService.sendTicketCreatedEmail(client.getUser().getEmail(), client.getUser().getFirstName(), sr.getTitle(), branding);
            } catch (Exception ex) {
                log.warn("Ticket created email failed for client {}: {}", client.getUser().getEmail(), ex.getMessage());
            }
        }

        ServiceRequestResponse response = toResponse(sr);

        if (agreedPrice.compareTo(BigDecimal.ZERO) > 0) {
            ClientInvoiceItemRequest item = ClientInvoiceItemRequest.builder()
                .description("Service Request: " + sr.getTitle())
                .quantity(new BigDecimal("1"))
                .unitPrice(agreedPrice)
                .build();

            ClientInvoiceRequest invoiceRequest = ClientInvoiceRequest.builder()
                .clientId(client.getId())
                .invoiceDate(java.time.LocalDate.now())
                .dueDate(java.time.LocalDate.now().plusDays(3))
                .notes("Invoice for Service Request: " + sr.getTitle())
                .items(List.of(item))
                .build();

            ClientInvoiceResponse invoiceResponse = invoiceService.create(invoiceRequest);
            sr.setInvoiceId(invoiceResponse.getId());
            serviceRequestRepository.save(sr);
            response.setInvoiceId(invoiceResponse.getId());
            
            invoiceService.sendInvoice(invoiceResponse.getId());
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestResponse getById(Long id) {
        return toResponse(findInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceRequestResponse> listAll(ServiceRequestStatus status, Pageable pageable) {
        Long companyId = requireCompanyId();
        Page<ServiceRequest> page = status != null
            ? serviceRequestRepository.findByCompanyIdAndStatus(companyId, status, pageable)
            : serviceRequestRepository.findByCompanyId(companyId, pageable);
        return page.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceRequestResponse> listMyRequests(Pageable pageable) {
        Long companyId = requireCompanyId();
        User currentUser = securityUtil.getCurrentUser();
        Client client = clientRepository.findByUserId(currentUser.getId())
            .orElseThrow(() -> new BadRequestException("Client profile not found"));
        return serviceRequestRepository
            .findByCompanyIdAndClientId(companyId, client.getId(), pageable)
            .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceRequestResponse> listAssignedToMe(Pageable pageable) {
        Long companyId = requireCompanyId();
        User currentUser = securityUtil.getCurrentUser();
        Employee emp = employeeRepository.findByUserId(currentUser.getId())
            .orElseThrow(() -> new BadRequestException("Employee profile not found"));
        return serviceRequestRepository
            .findByCompanyIdAndAssignedEmployeeId(companyId, emp.getId(), pageable)
            .map(this::toResponse);
    }

    @Override
    @Transactional
    public ServiceRequestResponse update(Long id, UpdateServiceRequestRequest request) {
        ServiceRequest sr = findInTenant(id);
        guardNotClosed(sr);

        if (request.getTitle()       != null) sr.setTitle(request.getTitle());
        if (request.getDescription() != null) sr.setDescription(request.getDescription());
        if (request.getPriority()    != null) sr.setPriority(request.getPriority());
        if (request.getAgreedPrice() != null) sr.setAgreedPrice(request.getAgreedPrice());
        if (request.getSlaDeadline() != null) sr.setSlaDeadline(request.getSlaDeadline());

        if (request.getAssignedEmployeeId() != null) {
            Employee emp = employeeRepository
                .findByIdAndCompanyId(request.getAssignedEmployeeId(), requireCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Employee not found: " + request.getAssignedEmployeeId()));
            sr.setAssignedEmployee(emp);
            if (sr.getAssignedAt() == null) sr.setAssignedAt(LocalDateTime.now());
        }
        return toResponse(sr);
    }

    @Override
    @Transactional
    public ServiceRequestResponse changeStatus(Long id, ChangeRequestStatusRequest request) {
        ServiceRequest sr = findInTenant(id);
        guardNotClosed(sr);

        ServiceRequestStatus oldStatus = sr.getStatus();
        ServiceRequestStatus newStatus = request.getStatus();
        User currentUser = securityUtil.getCurrentUser();

        sr.setStatus(newStatus);
        if (newStatus == ServiceRequestStatus.COMPLETED) {
            sr.setCompletedAt(LocalDateTime.now());
            automationEventPublisher.publishServiceRequestCompleted(
                this, requireCompanyId(), sr.getId(),
                sr.getClient() != null ? sr.getClient().getId() : null);
        }
        if (newStatus == ServiceRequestStatus.ASSIGNED && sr.getAssignedAt() == null)
            sr.setAssignedAt(LocalDateTime.now());

        recordStatusChange(sr, oldStatus, newStatus, request.getReason(),
            currentUser, requireCompanyId());
        notifyClientOnStatusChange(sr, newStatus);
        return toResponse(sr);
    }

    @Override
    @Transactional
    public ServiceRequestResponse assign(Long id, Long employeeId) {
        Long companyId = requireCompanyId();
        ServiceRequest sr = findInTenant(id);
        guardNotClosed(sr);

        Employee emp = employeeRepository.findByIdAndCompanyId(employeeId, companyId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Employee not found: " + employeeId));

        ServiceRequestStatus old = sr.getStatus();
        sr.setAssignedEmployee(emp);
        sr.setAssignedAt(LocalDateTime.now());
        sr.setStatus(ServiceRequestStatus.ASSIGNED);

        recordStatusChange(sr, old, ServiceRequestStatus.ASSIGNED,
            "Assigned to " + emp.getUser().getFullName(),
            securityUtil.getCurrentUser(), companyId);

        if (emp.getUser() != null) {
            notificationService.sendForServiceRequest(CreateNotificationRequest.forRequest(
                NotificationType.REQUEST_ASSIGNED,
                "Request Assigned",
                "Service request \"" + sr.getTitle() + "\" has been assigned to you.",
                emp.getUser().getId(), companyId, sr.getId()
            ));

            try {
                Company fullCompany = companyRepository.findById(companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
                EmailBranding.Data branding = emailBranding.from(fullCompany);
                emailService.sendTicketAssignedEmail(emp.getUser().getEmail(), emp.getUser().getFirstName(), sr.getTitle(), branding);
            } catch (Exception ex) {
                log.warn("Ticket assigned email failed for employee {}: {}", emp.getUser().getEmail(), ex.getMessage());
            }
        }
        return toResponse(sr);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        ServiceRequest sr = findInTenant(id);
        if (sr.getStatus() == ServiceRequestStatus.COMPLETED)
            throw new BadRequestException("Cannot cancel a completed request");

        ServiceRequestStatus oldStatus = sr.getStatus();
        sr.setStatus(ServiceRequestStatus.CANCELLED);
        sr.setPermanentlyClosed(true);
        recordStatusChange(sr, oldStatus, ServiceRequestStatus.CANCELLED,
            "Cancelled by platformuser", securityUtil.getCurrentUser(), requireCompanyId());
    }

    // ── Comments ──────────────────────────────────────────────────

    @Override
    @Transactional
    public RequestCommentResponse addComment(Long requestId, AddCommentRequest request) {
        Long companyId = requireCompanyId();
        ServiceRequest sr = findInTenant(requestId);
        User currentUser = securityUtil.getCurrentUser();

        RequestComment comment = RequestComment.builder()
            .content(request.getContent())
            .visibility(request.getVisibility() != null
                ? request.getVisibility() : CommentVisibility.INTERNAL)
            .attachmentUrl(request.getAttachmentUrl())
            .serviceRequest(sr)
            .company(companyRef(companyId))
            .author(currentUser)
            .build();

        commentRepository.save(comment);

        if (comment.getVisibility() == CommentVisibility.CLIENT
                && sr.getClient() != null
                && sr.getClient().getUser() != null) {
            notificationService.sendForServiceRequest(CreateNotificationRequest.forRequest(
                NotificationType.REQUEST_UPDATED,
                "New Comment",
                "A new update has been added to your request \"" + sr.getTitle() + "\".",
                sr.getClient().getUser().getId(), companyId, requestId
            ));
        }

        return ServiceRequestMapper.toCommentResponse(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RequestCommentResponse> getComments(Long requestId, Pageable pageable) {
        findInTenant(requestId);
        return commentRepository
            .findByServiceRequestIdOrderByCreatedAtDesc(requestId, pageable)
            .map(ServiceRequestMapper::toCommentResponse);
    }

    // ── Status history ────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<RequestStatusHistoryResponse> getStatusHistory(Long requestId) {
        findInTenant(requestId);
        return historyRepository
            .findByServiceRequestIdOrderByChangedAtAsc(requestId)
            .stream().map(ServiceRequestMapper::toHistoryResponse).toList();
    }

    @Override
    @Transactional
    public ServiceRequestResponse advanceStage(Long id) {
        ServiceRequest sr = findInTenant(id);
        guardNotClosed(sr);

        List<WorkflowStage> stages = loadWorkflowStages(sr);
        int current = sr.getCurrentStage() != null ? sr.getCurrentStage() : 0;
        if (current >= stages.size()) {
            throw new BadRequestException("Request is already at the final workflow stage");
        }

        WorkflowStage next = stages.get(current); // currentStage counts completed stages
        if (Boolean.TRUE.equals(next.getRequiresApproval())
            && !stageApprovalRepository.existsByServiceRequestIdAndWorkflowStageIdAndStatus(
                sr.getId(), next.getId(), ApprovalStatus.APPROVED)) {

            boolean alreadyPending = stageApprovalRepository
                .existsByServiceRequestIdAndWorkflowStageIdAndStatus(sr.getId(), next.getId(), ApprovalStatus.PENDING);
            if (!alreadyPending) {
                stageApprovalRepository.save(StageApproval.builder()
                    .serviceRequest(sr)
                    .workflowStage(next)
                    .approverRole(next.getAssigneeRole())
                    .requestedBy(securityUtil.getCurrentUser())
                    .company(sr.getCompany())
                    .build());
            }
            throw new BadRequestException("Stage \"" + next.getName()
                + "\" requires approval. An approval request is pending in the approvals queue.");
        }

        sr.setCurrentStage(current + 1);

        // Stage-level SLA: entering a stage with slaHours refreshes the deadline
        if (next.getSlaHours() != null && next.getSlaHours() > 0) {
            sr.setSlaHours(next.getSlaHours());
            sr.setSlaDeadline(LocalDateTime.now().plusHours(next.getSlaHours()));
            sr.setSlaBreach(false);
        }

        if (sr.getAssignedEmployee() != null && sr.getAssignedEmployee().getUser() != null) {
            notificationService.sendForServiceRequest(CreateNotificationRequest.forRequest(
                NotificationType.REQUEST_UPDATED,
                "Request moved to stage: " + next.getName(),
                "Request \"" + sr.getTitle() + "\" advanced to stage \"" + next.getName() + "\"",
                sr.getAssignedEmployee().getUser().getId(),
                requireCompanyId(),
                sr.getId()));
        }
        return toResponse(sr);
    }

    @Override
    @Transactional(readOnly = true)
    public StageProgressResponse getStageProgress(Long id) {
        ServiceRequest sr = findInTenant(id);
        List<WorkflowStage> stages = loadWorkflowStages(sr);
        int current = sr.getCurrentStage() != null ? sr.getCurrentStage() : 0;

        StageProgressResponse response = new StageProgressResponse();
        response.setServiceRequestId(sr.getId());
        response.setCurrentStage(current);
        response.setTotalStages(stages.size());
        response.setStages(stages.stream().map(stage -> {
            StageProgressResponse.StageItem item = new StageProgressResponse.StageItem();
            item.setStageId(stage.getId());
            item.setName(stage.getName());
            item.setStageOrder(stage.getStageOrder());
            item.setSlaHours(stage.getSlaHours());
            item.setRequiresApproval(stage.getRequiresApproval());
            int index = stages.indexOf(stage);
            item.setCompleted(index < current);
            item.setCurrent(index == current);
            if (Boolean.TRUE.equals(stage.getRequiresApproval())) {
                item.setApprovalStatus(resolveApprovalStatus(sr.getId(), stage.getId()));
            }
            return item;
        }).toList());
        return response;
    }

    private List<WorkflowStage> loadWorkflowStages(ServiceRequest sr) {
        if (sr.getCompanyService() == null || sr.getCompanyService().getWorkflowTemplate() == null) {
            throw new BadRequestException("This service has no workflow template configured");
        }
        List<WorkflowStage> stages = workflowStageRepository
            .findByWorkflowTemplateIdOrderByStageOrderAsc(sr.getCompanyService().getWorkflowTemplate().getId());
        if (stages.isEmpty()) {
            throw new BadRequestException("The workflow template has no stages configured");
        }
        return stages;
    }

    private String resolveApprovalStatus(Long serviceRequestId, Long stageId) {
        for (ApprovalStatus status : List.of(ApprovalStatus.APPROVED, ApprovalStatus.PENDING, ApprovalStatus.REJECTED)) {
            if (stageApprovalRepository.existsByServiceRequestIdAndWorkflowStageIdAndStatus(serviceRequestId, stageId, status)) {
                return status.name();
            }
        }
        return null;
    }

    private ServiceRequest findInTenant(Long id) {
        ServiceRequest sr = serviceRequestRepository.findByIdAndCompanyId(id, requireCompanyId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Service request not found: " + id));
        guardAccess(sr);
        return sr;
    }

    private Long requireCompanyId() {
        Long id = securityUtil.getCurrentCompanyId();
        if (id == null) throw new BadRequestException("No company context");
        return id;
    }

    private Company companyRef(Long companyId) {
        Company c = new Company();
        c.setId(companyId);
        return c;
    }

    private void guardNotClosed(ServiceRequest sr) {
        if (sr.isPermanentlyClosed())
            throw new BadRequestException("This request is permanently closed");
    }

    private void guardAccess(ServiceRequest sr) {
        User user = securityUtil.getCurrentUser();
        if (user == null || user.getRole() == null) return;
        
        String role = user.getRole().name();
        if (role.equals("COMPANY_OWNER") || role.equals("SYSTEM_ADMIN")) return;
        
        boolean isOwner = sr.getClient() != null 
                && sr.getClient().getUser() != null 
                && sr.getClient().getUser().getId().equals(user.getId());
                
        boolean isAssigned = sr.getAssignedEmployee() != null 
                && sr.getAssignedEmployee().getUser() != null 
                && sr.getAssignedEmployee().getUser().getId().equals(user.getId());
                
        if (!isOwner && !isAssigned) {
            throw new ForbiddenException("You do not have permission to access this service request");
        }
    }

    private void recordStatusChange(ServiceRequest sr, ServiceRequestStatus oldStatus,
                                     ServiceRequestStatus newStatus, String reason,
                                     User changedBy, Long companyId) {
        historyRepository.save(RequestStatusHistory.builder()
            .serviceRequest(sr)
            .oldStatus(oldStatus)
            .newStatus(newStatus)
            .reason(reason)
            .changedBy(changedBy)
            .companyId(companyId)
            .build());
    }

    private void notifyClientOnStatusChange(ServiceRequest sr,
                                             ServiceRequestStatus newStatus) {
        if (sr.getClient() == null || sr.getClient().getUser() == null) return;
        NotificationType type = switch (newStatus) {
            case COMPLETED      -> NotificationType.COMPLETED;
            case REJECTED       -> NotificationType.REJECTED;
            case CANCELLED      -> NotificationType.CANCELLED;
            case IN_PROGRESS    -> NotificationType.REQUEST_UPDATED;
            case WAITING_CLIENT -> NotificationType.REQUEST_UPDATED;
            default             -> null;
        };
        if (type == null) return;
        notificationService.sendForServiceRequest(CreateNotificationRequest.forRequest(
            type, "Request Update",
            "Your request \"" + sr.getTitle() + "\" is now "
                + newStatus.name().replace('_', ' ') + ".",
            sr.getClient().getUser().getId(),
            sr.getCompany().getId(),
            sr.getId()
        ));

        if (newStatus == ServiceRequestStatus.COMPLETED) {
            try {
                Company fullCompany = companyRepository.findById(sr.getCompany().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
                EmailBranding.Data branding = emailBranding.from(fullCompany);
                emailService.sendTicketResolvedEmail(sr.getClient().getUser().getEmail(), sr.getClient().getUser().getFirstName(), sr.getTitle(), branding);
            } catch (Exception ex) {
                log.warn("Ticket resolved email failed for client {}: {}", sr.getClient().getUser().getEmail(), ex.getMessage());
            }
        }
    }

    private ServiceRequestResponse toResponse(ServiceRequest sr) {
        long taskCount = taskRepository.countByServiceRequestId(sr.getId());
        long completedCount = taskRepository.countByServiceRequestIdAndStatus(
            sr.getId(), TaskStatus.COMPLETED);
        return ServiceRequestMapper.toResponse(sr, taskCount, completedCount);
    }

    @Override
    @Transactional
    public ServiceRequestResponse submitQuotation(Long id, SubmitQuotationRequest request) {
        ServiceRequest sr = serviceRequestRepository.findByIdAndCompanyId(id, securityUtil.getCurrentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));
        
        sr.submitQuotation(request.getAmount(), request.getCurrency(), request.getNotes(), request.getValidUntil());
        sr = serviceRequestRepository.save(sr);
        return toResponse(sr);
    }

    @Override
    @Transactional
    public ServiceRequestResponse acceptQuotation(Long id) {
        ServiceRequest sr = serviceRequestRepository.findByIdAndCompanyId(id, securityUtil.getCurrentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));
        
        if (sr.getQuotationStatus() != com.businessos.enums.QuotationStatus.PENDING) {
            throw new BadRequestException("Only pending quotations can be accepted.");
        }
        
        sr.acceptQuotation();
        sr = serviceRequestRepository.save(sr);
        return toResponse(sr);
    }

    @Override
    @Transactional
    public ServiceRequestResponse rejectQuotation(Long id, RejectQuotationRequest request) {
        ServiceRequest sr = serviceRequestRepository.findByIdAndCompanyId(id, securityUtil.getCurrentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));
        
        if (sr.getQuotationStatus() != com.businessos.enums.QuotationStatus.PENDING) {
            throw new BadRequestException("Only pending quotations can be rejected.");
        }
        
        sr.rejectQuotation(request.getReason());
        sr = serviceRequestRepository.save(sr);
        return toResponse(sr);
    }
}

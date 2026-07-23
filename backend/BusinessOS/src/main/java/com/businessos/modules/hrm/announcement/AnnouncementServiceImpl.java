package com.businessos.modules.hrm.announcement;

import com.businessos.shared.notification.CreateNotificationRequest;
import com.businessos.modules.ai.enums.AiFeature;
import com.businessos.modules.ai.prompt.AnnouncementDraftPromptBuilder;
import com.businessos.modules.ai.service.AiService;
import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.modules.hrm.department.Department;
import com.businessos.enums.AnnouncementAudience;
import com.businessos.enums.NotificationType;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.modules.hrm.department.DepartmentRepository;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.notification.NotificationService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor

public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationService notificationService;
    private final SecurityUtil securityUtil;
    private final AuthorizationService authorizationService;
    private final CompanyRepository companyRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AnnouncementResponse create(AnnouncementRequest request) {
        authorizationService.checkPermission(PermissionCode.ANNOUNCEMENT_CREATE);
        Long companyId = requireCompanyId();

        Department targetDept = null;
        if (request.getTargetDepartmentId() != null) {
            targetDept = departmentRepository.findByIdAndCompanyId(request.getTargetDepartmentId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Department not found: " + request.getTargetDepartmentId()));
        }

        Announcement announcement = Announcement.builder()
            .title(request.getTitle())
            .body(request.getBody())
            .audience(request.getAudience() != null ? request.getAudience() : AnnouncementAudience.ALL)
            .targetDepartment(targetDept)
            .expiresAt(request.getExpiresAt())
            .notifyAll(request.isNotifyAll())
            .priority(request.getPriority() != null ? request.getPriority() : 0)
            .attachmentUrl(request.getAttachmentUrl())
            .published(false)
            .company(companyRef(companyId))
            .createdBy(securityUtil.getCurrentUser())
            .build();

        announcementRepository.save(announcement);
        return AnnouncementMapper.toAnnouncementResponse(announcement);
    }

    @Override
    @Transactional(readOnly = true)
    public AnnouncementResponse getById(Long id) {
        return AnnouncementMapper.toAnnouncementResponse(findInTenant(id));
    }



    @Override
    @Transactional(readOnly = true)
    public Page<AnnouncementResponse> listAll(Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.ANNOUNCEMENT_VIEW);
        return announcementRepository.findByCompanyId(requireCompanyId(), pageable)
            .map(AnnouncementMapper::toAnnouncementResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementResponse> listActive() {
        authorizationService.checkPermission(PermissionCode.ANNOUNCEMENT_VIEW);
        return announcementRepository.findActiveByCompanyId(requireCompanyId(), LocalDateTime.now())
            .stream().map(AnnouncementMapper::toAnnouncementResponse).toList();
    }

    @Override
    @Transactional
    public AnnouncementResponse publish(Long id) {
        authorizationService.checkPermission(PermissionCode.ANNOUNCEMENT_UPDATE);
        Long companyId = requireCompanyId();
        Announcement announcement = findInTenant(id);
        if (announcement.isPublished()) throw new BadRequestException("Announcement is already published");
        announcement.setPublished(true);
        announcement.setPublishedAt(LocalDateTime.now());

        if (announcement.isNotifyAll()) {
            int pageNum = 0;
            final int PAGE_SIZE = 100;
            org.springframework.data.domain.Page<com.businessos.modules.hrm.employee.Employee> page;
            do {
                // Filter by department if audience = DEPARTMENT
                if (announcement.getAudience() == AnnouncementAudience.DEPARTMENT
                        && announcement.getTargetDepartment() != null) {
                    page = employeeRepository.findByCompanyIdAndDepartmentId(
                        companyId, announcement.getTargetDepartment().getId(),
                        PageRequest.of(pageNum, PAGE_SIZE));
                } else {
                    page = employeeRepository.findByCompanyId(companyId,
                        PageRequest.of(pageNum, PAGE_SIZE));
                }
                page.getContent().forEach(emp -> {
                    if (emp.getUser() != null) {
                        notificationService.send(CreateNotificationRequest.of(
                            NotificationType.ANNOUNCEMENT,
                            announcement.getTitle(),
                            announcement.getBody().length() > 150
                                ? announcement.getBody().substring(0, 147) + "..."
                                : announcement.getBody(),
                            "/announcements/" + announcement.getId(),
                            emp.getUser().getId(),
                            companyId
                        ));
                    }
                });
                pageNum++;
            } while (page.hasNext());
        }
        
        return AnnouncementMapper.toAnnouncementResponse(announcement);
    }

    @Override
    @Transactional
    public AnnouncementResponse update(Long id, AnnouncementRequest request) {
        authorizationService.checkPermission(PermissionCode.ANNOUNCEMENT_UPDATE);
        Long companyId = requireCompanyId();
        Announcement announcement = findInTenant(id);
        if (announcement.isPublished()) throw new BadRequestException("Cannot edit a published announcement");
        if (request.getTitle()!= null) announcement.setTitle(request.getTitle());
        if (request.getBody()!= null) announcement.setBody(request.getBody());
        if (request.getAudience()!= null) announcement.setAudience(request.getAudience());
        if (request.getAttachmentUrl()!= null) announcement.setAttachmentUrl(request.getAttachmentUrl());
        if (request.getPriority()!= null) announcement.setPriority(request.getPriority());
        announcement.setExpiresAt(request.getExpiresAt());
        announcement.setNotifyAll(request.isNotifyAll());
        if (request.getTargetDepartmentId() != null) {
            announcement.setTargetDepartment(
                departmentRepository.findByIdAndCompanyId(request.getTargetDepartmentId(), companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found")));
        }
        return AnnouncementMapper.toAnnouncementResponse(announcement);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        authorizationService.checkPermission(PermissionCode.ANNOUNCEMENT_DELETE);
        Announcement a = findInTenant(id);
        if (a.isPublished()) throw new BadRequestException("Cannot delete a published announcement");
        a.softDelete();
    }

    @Override
    @Transactional(readOnly = true)
    public AnnouncementDraftResponse draftWithAi(AnnouncementDraftRequest request) {
        authorizationService.checkPermission(PermissionCode.ANNOUNCEMENT_CREATE);
        Long companyId = requireCompanyId();
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));

        String prompt = AnnouncementDraftPromptBuilder.builder()
            .setCompanyName(company.getCompanyName())
            .setToday(LocalDate.now())
            .setInstructions(request.getInstructions())
            .build();

        String raw = aiService.generateFromPrompt(AiFeature.ANNOUNCEMENT_DRAFT, prompt);
        return parseDraft(raw, request.getInstructions());
    }

    private AnnouncementDraftResponse parseDraft(String raw, String fallbackInstructions) {
        AnnouncementDraftResponse response = new AnnouncementDraftResponse();
        try {
            String cleaned = raw.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceFirst("^```[a-zA-Z]*\\n?", "").replaceFirst("```\\s*$", "");
            }
            JsonNode node = objectMapper.readTree(cleaned);
            response.setTitle(node.path("title").asText(null));
            response.setBody(node.path("body").asText(null));
        } catch (Exception ignored) {
            // Model didn't return valid JSON despite instructions - fall back to the
            // raw text as the body rather than failing the whole request.
        }
        if (response.getTitle() == null || response.getTitle().isBlank()) {
            response.setTitle(fallbackInstructions.length() > 80
                ? fallbackInstructions.substring(0, 77) + "..." : fallbackInstructions);
        }
        if (response.getBody() == null || response.getBody().isBlank()) {
            response.setBody(raw);
        }
        return response;
    }

    private Announcement findInTenant(Long id) {
        return announcementRepository.findByIdAndCompanyId(id, requireCompanyId())
            .orElseThrow(() -> new ResourceNotFoundException("Announcement not found: " + id));
    }

    private Long requireCompanyId() {
        Long id = securityUtil.getCurrentCompanyId();
        if (id == null) throw new BadRequestException("No company context");
        return id;
    }

    private Company companyRef(Long companyId) {
        Company c = new Company(); c.setId(companyId); return c;
    }
}


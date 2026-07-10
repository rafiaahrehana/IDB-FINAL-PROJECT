package com.businessos.modules.servicedesk.workflow.workflow;

import com.businessos.modules.servicedesk.workflow.stage.WorkflowStage;
import com.businessos.modules.servicedesk.workflow.stage.WorkflowStageRepository;
import com.businessos.modules.servicedesk.workflow.stage.WorkflowStageRequest;
import com.businessos.modules.servicedesk.workflow.stage.WorkflowStageResponse;
import com.businessos.modules.servicedesk.workflow.template.WorkflowTemplate;
import com.businessos.modules.servicedesk.workflow.template.WorkflowTemplateRepository;
import com.businessos.modules.servicedesk.workflow.template.WorkflowTemplateRequest;
import com.businessos.modules.servicedesk.workflow.template.WorkflowTemplateResponse;
import com.businessos.modules.company.Company;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.security.SecurityUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor

public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowTemplateRepository templateRepository;
    private final WorkflowStageRepository stageRepository;
    private final SecurityUtil               securityUtil;

    // ── Templates ─────────────────────────────────────────────────

    @Override
    @Transactional
    public WorkflowTemplateResponse createTemplate(WorkflowTemplateRequest request) {
        Long companyId = requireCompanyId();
        if (templateRepository.existsByCompanyIdAndName(companyId, request.getName())) {
            throw new BadRequestException(
                    "A workflow template named '" + request.getName() + "' already exists");
        }
        WorkflowTemplate template = WorkflowTemplate.builder()
                .name(request.getName())
                .description(request.getDescription())
                .company(companyRef(companyId))
                .version(1)
                .active(true)
                .build();

        templateRepository.save(template);
        
        return WorkflowMapper.toResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowTemplateResponse getTemplateById(Long id) {
        return WorkflowMapper.toResponse(findTemplate(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkflowTemplateResponse> listTemplates(Pageable pageable) {
        Long companyId = requireCompanyId();
        /*
         * BUG-FIX: was templateRepository.findAll(pageable) — leaked ALL tenants' templates.
         * Fixed to findByCompanyId() to enforce tenant isolation.
         */
        return templateRepository.findByCompanyId(companyId, pageable)
                .map(WorkflowMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowTemplateResponse> listActiveTemplates() {
        Long companyId = requireCompanyId();
        /*
         * BUG-FIX: was templateRepository.findByActiveTrue() — leaked cross-tenant data.
         * Fixed to findByCompanyIdAndActiveTrue().
         */
        return templateRepository.findByCompanyIdAndActiveTrue(companyId)
                .stream()
                .map(WorkflowMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public WorkflowTemplateResponse updateTemplate(Long id, WorkflowTemplateRequest request) {
        Long companyId = requireCompanyId();
        WorkflowTemplate template = findTemplate(id);

        if (!template.getName().equals(request.getName())
                && templateRepository.existsByCompanyIdAndName(companyId, request.getName())) {
            throw new BadRequestException(
                    "A workflow template named '" + request.getName() + "' already exists");
        }

        template.setName(request.getName());
        if (request.getDescription() != null)
            template.setDescription(request.getDescription());
        template.setVersion(template.getVersion() + 1);

        templateRepository.save(template);
        return WorkflowMapper.toResponse(template);
    }

    @Override
    @Transactional
    public WorkflowTemplateResponse toggleTemplate(Long id) {
        WorkflowTemplate template = findTemplate(id);

        if (template.isActive()) {
            boolean hasActiveStages = template.getStages().stream()
                    .anyMatch(s -> !s.isDeleted());
            if (hasActiveStages) {
                
            }
        }

        template.setActive(!template.isActive());
        templateRepository.save(template);
        
        return WorkflowMapper.toResponse(template);
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        WorkflowTemplate template = findTemplate(id);

        if (!template.getStages().isEmpty()) {
            throw new BadRequestException(
                    "Cannot delete a workflow template that still has stages. Remove all stages first.");
        }

        template.softDelete();
        templateRepository.save(template);
        
    }

    // ── Stages ────────────────────────────────────────────────────

    @Override
    @Transactional
    public WorkflowStageResponse addStage(Long templateId, WorkflowStageRequest request) {
        Long companyId = requireCompanyId();
        WorkflowTemplate template = findTemplate(templateId);

        if (stageRepository.existsByWorkflowTemplateIdAndStageOrder(
                templateId, request.getStageOrder())) {
            throw new BadRequestException(
                    "Stage order " + request.getStageOrder() + " is already taken in this workflow");
        }

        WorkflowStage stage = WorkflowStage.builder()
                .name(request.getName())
                .description(request.getDescription())
                .stageOrder(request.getStageOrder())
                .estimatedDays(request.getEstimatedDays())
                .slaHours(request.getSlaHours())
                .requiresApproval(request.getRequiresApproval())
                .assigneeRole(request.getAssigneeRole())
                .workflowTemplate(template)
                .company(companyRef(companyId))
                .build();

        stageRepository.save(stage);
        template.setVersion(template.getVersion() + 1);
        templateRepository.save(template);

        
        return WorkflowMapper.toStageResponse(stage);
    }

    @Override
    @Transactional
    public WorkflowStageResponse updateStage(Long templateId, Long stageId,
                                             WorkflowStageRequest request) {
        findTemplate(templateId);

        WorkflowStage stage = stageRepository.findById(stageId)
                .filter(s -> s.getWorkflowTemplate().getId().equals(templateId))
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found: " + stageId));

        if (!stage.getStageOrder().equals(request.getStageOrder())
                && stageRepository.existsByWorkflowTemplateIdAndStageOrder(
                templateId, request.getStageOrder())) {
            throw new BadRequestException(
                    "Stage order " + request.getStageOrder() + " is already taken");
        }

        stage.setName(request.getName());
        stage.setStageOrder(request.getStageOrder());
        stage.setEstimatedDays(request.getEstimatedDays());
        stage.setSlaHours(request.getSlaHours());
        stage.setRequiresApproval(request.getRequiresApproval());
        stage.setAssigneeRole(request.getAssigneeRole());
        if (request.getDescription() != null)
            stage.setDescription(request.getDescription());

        stageRepository.save(stage);

        WorkflowTemplate template = stage.getWorkflowTemplate();
        template.setVersion(template.getVersion() + 1);
        templateRepository.save(template);

        return WorkflowMapper.toStageResponse(stage);
    }

    @Override
    @Transactional
    public void deleteStage(Long templateId, Long stageId) {
        WorkflowTemplate template = findTemplate(templateId);

        WorkflowStage stage = stageRepository.findById(stageId)
                .filter(s -> s.getWorkflowTemplate().getId().equals(templateId))
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found: " + stageId));

        stage.softDelete();
        stageRepository.save(stage);

        template.setVersion(template.getVersion() + 1);
        templateRepository.save(template);

        
    }

    // ── Private helpers ───────────────────────────────────────────

    private WorkflowTemplate findTemplate(Long id) {
        return templateRepository.findByIdAndCompanyId(id, requireCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workflow template not found: " + id));
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
}

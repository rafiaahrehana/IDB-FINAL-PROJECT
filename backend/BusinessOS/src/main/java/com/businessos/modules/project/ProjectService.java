package com.businessos.modules.project;

import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.auth.user.User;
import com.businessos.auth.user.UserRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    private Long requireCompanyId() {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null) throw new BadRequestException("No company context found.");
        return companyId;
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        Long companyId = requireCompanyId();
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new BadRequestException("Company not found"));

        Project project = Project.builder()
            .company(company)
            .name(request.getName())
            .description(request.getDescription())
            .status(request.getStatus() != null ? request.getStatus() : ProjectStatus.PLANNING)
            .priority(request.getPriority() != null ? request.getPriority() : com.businessos.enums.Priority.NORMAL)
            .owner(resolveUser(request.getOwnerId()))
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .progress(request.getProgress() != null ? request.getProgress() : 0)
            .budget(request.getBudget())
            .build();
        return toDTO(projectRepository.save(project));
    }

    public Page<ProjectResponse> list(Pageable pageable) {
        return projectRepository.findByCompanyId(requireCompanyId(), pageable).map(this::toDTO);
    }

    public ProjectResponse getById(Long id) {
        return toDTO(projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id)));
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        if (request.getStatus() != null) project.setStatus(request.getStatus());
        if (request.getPriority() != null) project.setPriority(request.getPriority());
        project.setOwner(resolveUser(request.getOwnerId()));
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        if (request.getProgress() != null) project.setProgress(request.getProgress());
        project.setBudget(request.getBudget());
        return toDTO(projectRepository.save(project));
    }

    @Transactional
    public void delete(Long id) {
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        project.softDelete();
        projectRepository.save(project);
    }

    public long getActiveCount() {
        return projectRepository.countByCompanyIdAndStatus(requireCompanyId(), ProjectStatus.ACTIVE);
    }

    public double getAverageProgress() {
        return projectRepository.averageProgress(requireCompanyId());
    }

    public long getTotalCount() {
        return projectRepository.countByCompanyId(requireCompanyId());
    }

    private ProjectResponse toDTO(Project p) {
        ProjectResponse r = new ProjectResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setDescription(p.getDescription());
        r.setStatus(p.getStatus());
        r.setPriority(p.getPriority());
        r.setOwnerId(p.getOwner() != null ? p.getOwner().getId() : null);
        r.setStartDate(p.getStartDate());
        r.setEndDate(p.getEndDate());
        r.setProgress(p.getProgress());
        r.setBudget(p.getBudget());
        return r;
    }

    private User resolveUser(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}

package com.businessos.modules.project.task;

import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.modules.project.Project;
import com.businessos.modules.project.ProjectRepository;
import com.businessos.enums.TaskStatus;
import com.businessos.enums.Priority;
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
public class TaskService {

    @org.springframework.beans.factory.annotation.Qualifier("projectTaskRepository")
    private final TaskRepository taskRepository;
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
    public TaskResponse create(TaskRequest request) {
        Long companyId = requireCompanyId();
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new BadRequestException("Company not found"));

        Task task = Task.builder()
            .company(company)
            .title(request.getTitle())
            .description(request.getDescription())
            .status(request.getStatus() != null ? request.getStatus() : TaskStatus.PENDING)
            .priority(request.getPriority() != null ? request.getPriority() : Priority.NORMAL)
            .assignee(resolveUser(request.getAssigneeId()))
            .dueDate(request.getDueDate())
            .build();

        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.getProjectId()));
            task.setProject(project);
        }
        return toDTO(taskRepository.save(task));
    }

    public Page<TaskResponse> list(Pageable pageable) {
        return taskRepository.findByCompanyId(requireCompanyId(), pageable).map(this::toDTO);
    }

    public TaskResponse getById(Long id) {
        return toDTO(taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id)));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        task.setAssignee(resolveUser(request.getAssigneeId()));
        task.setDueDate(request.getDueDate());
        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.getProjectId()));
            task.setProject(project);
        }
        return toDTO(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
        task.softDelete();
        taskRepository.save(task);
    }

    public long getOpenCount() {
        return taskRepository.countByCompanyIdAndStatus(requireCompanyId(), TaskStatus.PENDING)
            + taskRepository.countByCompanyIdAndStatus(requireCompanyId(), TaskStatus.IN_PROGRESS)
            + taskRepository.countByCompanyIdAndStatus(requireCompanyId(), TaskStatus.BLOCKED);
    }

    public long getTotalCount() {
        return taskRepository.countByCompanyId(requireCompanyId());
    }

    public long countByStatus(TaskStatus status) {
        return taskRepository.countByCompanyIdAndStatus(requireCompanyId(), status);
    }

    private TaskResponse toDTO(Task t) {
        TaskResponse r = new TaskResponse();
        r.setId(t.getId());
        r.setTitle(t.getTitle());
        r.setDescription(t.getDescription());
        r.setStatus(t.getStatus());
        r.setPriority(t.getPriority());
        r.setProjectId(t.getProject() != null ? t.getProject().getId() : null);
        r.setAssigneeId(t.getAssignee() != null ? t.getAssignee().getId() : null);
        r.setDueDate(t.getDueDate());
        return r;
    }

    private User resolveUser(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}

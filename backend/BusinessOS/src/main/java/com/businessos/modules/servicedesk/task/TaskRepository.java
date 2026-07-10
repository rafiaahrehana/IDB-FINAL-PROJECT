package com.businessos.modules.servicedesk.task;

import com.businessos.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByIdAndCompanyId(Long id, Long companyId);

    List<Task> findByServiceRequestIdOrderByCreatedAtAsc(Long serviceRequestId);

    Page<Task> findByCompanyIdAndAssignedEmployeeId(
        Long companyId, Long employeeId, Pageable pageable);

    Page<Task> findByCompanyIdAndStatus(Long companyId, TaskStatus status, Pageable pageable);

    long countByServiceRequestIdAndStatus(Long serviceRequestId, TaskStatus status);

    long countByServiceRequestId(Long serviceRequestId);
}

package com.businessos.modules.project.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.businessos.enums.TaskStatus;

@Repository("projectTaskRepository")
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByCompanyId(Long companyId, Pageable pageable);

    long countByCompanyId(Long companyId);

    long countByCompanyIdAndStatus(Long companyId, TaskStatus status);
}

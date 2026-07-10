package com.businessos.modules.project.task;

import com.businessos.core.base.BaseEntity;
import com.businessos.modules.company.Company;
import com.businessos.auth.user.User;
import com.businessos.modules.project.Project;
import com.businessos.enums.TaskStatus;
import com.businessos.enums.Priority;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDate;

@Entity(name = "ProjectTask")
@Table(
    name = "tasks",
    indexes = {
        @Index(name = "idx_task_company", columnList = "company_id"),
        @Index(name = "idx_task_status", columnList = "company_id, status")
    }
)
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "companyId", type = Long.class))
@Filter(name = "tenantFilter", condition = "company_id = :companyId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Column(name = "due_date")
    private LocalDate dueDate;
}

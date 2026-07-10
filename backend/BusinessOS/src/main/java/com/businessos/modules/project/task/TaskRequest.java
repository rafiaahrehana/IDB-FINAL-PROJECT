package com.businessos.modules.project.task;

import com.businessos.enums.Priority;
import com.businessos.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    private TaskStatus status;

    private Priority priority;

    private Long projectId;

    private Long assigneeId;

    private LocalDate dueDate;
}

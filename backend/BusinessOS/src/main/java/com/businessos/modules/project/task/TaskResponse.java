package com.businessos.modules.project.task;

import com.businessos.enums.Priority;
import com.businessos.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private Long projectId;
    private Long assigneeId;
    private java.time.LocalDate dueDate;
}

package com.businessos.modules.project;

import com.businessos.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    private String name;

    private String description;

    private ProjectStatus status;

    private Priority priority;

    private Long ownerId;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer progress;

    private BigDecimal budget;
}

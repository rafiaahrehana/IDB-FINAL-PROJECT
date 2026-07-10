package com.businessos.modules.project;

import com.businessos.enums.Priority;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private Priority priority;
    private Long ownerId;
    private String ownerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int progress;
    private BigDecimal budget;
    private String companyId;
}

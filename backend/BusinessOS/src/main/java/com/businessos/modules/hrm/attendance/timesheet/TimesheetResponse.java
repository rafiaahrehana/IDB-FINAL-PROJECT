package com.businessos.modules.hrm.attendance.timesheet;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class TimesheetResponse {
    private Long id;
    private LocalDate workDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double hoursWorked;
    private double billableHours;
    private String projectName;
    private String taskDescription;
    private String description;
    private boolean approved;
    private LocalDateTime approvedAt;
    private Long employeeId;
    private String employeeName;
    private Long approvedById;
    private String approvedByName;
    private Long taskId;
    private String taskTitle;
    private LocalDateTime createdAt;
}

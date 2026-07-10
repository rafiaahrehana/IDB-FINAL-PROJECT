package com.businessos.modules.hrm.attendance.leave;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceLeaveResponse {
    private Long id;
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private LocalDate leaveDate;
    private String leaveType;
    private String leaveReason;
    private boolean halfDay;
    private boolean approved;
    private String approvedBy;
    private LocalDate approvedDate;
    private String rejectionReason;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

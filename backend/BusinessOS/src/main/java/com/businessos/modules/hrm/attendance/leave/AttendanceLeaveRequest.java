package com.businessos.modules.hrm.attendance.leave;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceLeaveRequest {
    @NotNull
    private Long employeeId;
    @NotNull
    private LocalDate leaveDate;
    private String leaveType;
    private String leaveReason;
    private boolean halfDay;
    private String notes;
}

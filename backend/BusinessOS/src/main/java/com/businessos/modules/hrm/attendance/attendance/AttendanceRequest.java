package com.businessos.modules.hrm.attendance.attendance;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    private LocalTime checkInTime;
    private LocalTime checkOutTime;

    private AttendanceMethod checkInMethod;
    private AttendanceMethod checkOutMethod;

    @NotNull(message = "Status is required")
    private AttendanceStatus status;

    private ShiftType shiftType;

    private boolean isLate;
    private long lateMinutes;
    private String lateReason;

    private boolean isOvertime;
    private java.math.BigDecimal overtimeHours;

    private boolean leftEarly;
    private long earlyMinutes;
    private String earlyDepartureReason;

    private String adminNotes;
}

package com.businessos.modules.hrm.attendance.attendance;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponse {

    private Long id;
    private Long companyId;

    // Employee info
    private Long employeeId;
    private String employeeName;
    private String employeeNumber;

    // Date
    private LocalDate attendanceDate;

    // Check-in
    private LocalTime checkInTime;
    private LocalDateTime checkInDateTime;
    private AttendanceMethod checkInMethod;
    private String checkInLocation;
    private String checkInLatitude;
    private String checkInLongitude;
    private String checkInReason;

    // Check-out
    private LocalTime checkOutTime;
    private LocalDateTime checkOutDateTime;
    private AttendanceMethod checkOutMethod;
    private String checkOutLocation;

    // Status
    private AttendanceStatus status;
    private ShiftType shiftType;

    // Late tracking
    private boolean isLate;
    private long lateMinutes;
    private String lateReason;

    // Overtime tracking
    private boolean isOvertime;
    private BigDecimal overtimeHours;

    // Early departure
    private boolean leftEarly;
    private long earlyMinutes;
    private String earlyDepartureReason;

    // Hours
    private BigDecimal totalWorkingHours;

    // Biometric
    private boolean isVerified;
    private double verificationScore;

    // Approval
    private boolean approved;
    private String approvedBy;
    private LocalDateTime approvedDateTime;

    // Notes
    private String notes;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

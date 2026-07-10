package com.businessos.modules.hrm.attendance.attendance;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceCheckInRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Check-in time is required")
    private LocalTime checkInTime;

    @NotNull(message = "Attendance method is required")
    private AttendanceMethod method;

    private Long deviceId; // If biometric device
    private String latitude; // If GPS
    private String longitude;
    private String location;

    private String reason;
    private boolean verified; // Biometric verification result
    private double verificationScore; // Match score
}
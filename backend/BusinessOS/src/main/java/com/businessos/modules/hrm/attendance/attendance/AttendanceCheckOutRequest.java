package com.businessos.modules.hrm.attendance.attendance;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceCheckOutRequest {

    @NotNull(message = "Check-out time is required")
    private LocalTime checkOutTime;

    @NotNull(message = "Attendance method is required")
    private AttendanceMethod method;

    private Long deviceId;       // If biometric device used for check-out

    private String latitude;     // If GPS-based check-out
    private String longitude;
    private String location;

    private String earlyDepartureReason;  // Optional reason if leaving before shift end
}

package com.businessos.modules.hrm.attendance.biometric.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiometricDeviceRequest {

    @NotBlank(message = "Device name is required")
    private String deviceName;

    @NotNull(message = "Device type is required")
    private BiometricDeviceType deviceType;

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    private String ipAddress;
    private int portNumber;

    private String location;
    private String department;

    private Integer matchThreshold;
    private Boolean enabledForCheckIn;
    private Boolean enabledForCheckOut;

    private String notes;
}

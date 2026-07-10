package com.businessos.modules.hrm.attendance.capture.strategy;

import com.businessos.modules.hrm.attendance.attendance.AttendanceCheckInRequest;
import com.businessos.modules.hrm.attendance.attendance.AttendanceCheckOutRequest;
import com.businessos.modules.hrm.attendance.attendance.Attendance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FingerprintAttendanceCapture implements AttendanceCaptureStrategy {

    @Override
    public Attendance captureCheckIn(AttendanceCheckInRequest request) {
        // Call fingerprint device API
        // Match biometric template with enrolled data
        // Return attendance with verification score
        return null; // Simplified
    }

    @Override
    public Attendance captureCheckOut(AttendanceCheckOutRequest request) {
        // Similar to check-in
        return null;
    }

    @Override
    public boolean verifyBiometric(String template, double threshold) {
        // Match template against enrolled fingerprints
        // Return true if match > threshold (95%)
        return false;
    }

    @Override
    public String getMethodName() {
        return "FINGERPRINT";
    }
}

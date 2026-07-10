package com.businessos.modules.hrm.attendance.capture.strategy;

import com.businessos.modules.hrm.attendance.attendance.AttendanceCheckInRequest;
import com.businessos.modules.hrm.attendance.attendance.AttendanceCheckOutRequest;
import com.businessos.modules.hrm.attendance.attendance.Attendance;
import org.springframework.stereotype.Component;

@Component
public class ManualAttendanceCapture implements AttendanceCaptureStrategy {

    @Override
    public Attendance captureCheckIn(AttendanceCheckInRequest request) {
        // HR/Admin manually marks attendance
        // No verification needed
        return null;
    }

    @Override
    public Attendance captureCheckOut(AttendanceCheckOutRequest request) {
        // Manual check-out
        return null;
    }

    @Override
    public boolean verifyBiometric(String template, double threshold) {
        return true; // No verification for manual
    }

    @Override
    public String getMethodName() {
        return "MANUAL";
    }
}
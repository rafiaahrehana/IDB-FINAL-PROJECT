package com.businessos.modules.hrm.attendance.capture.context;

import com.businessos.modules.hrm.attendance.attendance.AttendanceCheckInRequest;
import com.businessos.modules.hrm.attendance.attendance.Attendance;
import com.businessos.modules.hrm.attendance.attendance.AttendanceMethod;
import com.businessos.modules.hrm.attendance.capture.strategy.AttendanceCaptureStrategy;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class AttendanceCaptureContext {

    private final Map<String, AttendanceCaptureStrategy> strategies;

    public AttendanceCaptureContext(Map<String, AttendanceCaptureStrategy> strategies) {
        this.strategies = strategies;
    }

    public Attendance captureCheckIn(AttendanceCheckInRequest request, AttendanceMethod method) {
        String strategyName = method.name().toLowerCase();
        AttendanceCaptureStrategy strategy = strategies.get(strategyName + "AttendanceCapture");

        if (strategy == null) {
            throw new IllegalArgumentException("Strategy not found for method: " + method);
        }

        return strategy.captureCheckIn(request);
    }
}
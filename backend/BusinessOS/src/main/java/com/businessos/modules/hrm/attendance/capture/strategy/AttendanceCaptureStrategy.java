package com.businessos.modules.hrm.attendance.capture.strategy;

import com.businessos.modules.hrm.attendance.attendance.AttendanceCheckInRequest;
import com.businessos.modules.hrm.attendance.attendance.AttendanceCheckOutRequest;
import com.businessos.modules.hrm.attendance.attendance.Attendance;

public interface AttendanceCaptureStrategy {

    Attendance captureCheckIn(AttendanceCheckInRequest request);
    Attendance captureCheckOut(AttendanceCheckOutRequest request);
    boolean verifyBiometric(String template, double threshold);
    String getMethodName();
}
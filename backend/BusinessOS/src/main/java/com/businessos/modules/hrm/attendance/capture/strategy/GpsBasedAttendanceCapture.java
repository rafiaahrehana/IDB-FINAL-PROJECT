package com.businessos.modules.hrm.attendance.capture.strategy;

import com.businessos.modules.hrm.attendance.attendance.AttendanceCheckInRequest;
import com.businessos.modules.hrm.attendance.attendance.AttendanceCheckOutRequest;
import com.businessos.modules.hrm.attendance.attendance.Attendance;
import org.springframework.stereotype.Component;

@Component
public class GpsBasedAttendanceCapture implements AttendanceCaptureStrategy {

    private static final double OFFICE_LAT = 23.8103;
    private static final double OFFICE_LNG = 90.4125;
    private static final double RADIUS_KM = 0.5; // 500 meters

    @Override
    public Attendance captureCheckIn(AttendanceCheckInRequest request) {
        // Verify GPS location is within office radius
        boolean isWithinRadius = isWithinOfficeLocation(
                Double.parseDouble(request.getLatitude()),
                Double.parseDouble(request.getLongitude())
        );

        if (isWithinRadius) {
            // Mark attendance
        }
        return null;
    }

    @Override
    public Attendance captureCheckOut(AttendanceCheckOutRequest request) {
        return null;
    }

    @Override
    public boolean verifyBiometric(String template, double threshold) {
        return true; // No biometric for GPS
    }

    @Override
    public String getMethodName() {
        return "GPS";
    }

    private boolean isWithinOfficeLocation(double lat, double lng) {
        double distance = calculateDistance(lat, lng, OFFICE_LAT, OFFICE_LNG);
        return distance <= RADIUS_KM;
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // Haversine formula
        double R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}

package com.businessos.modules.hrm.attendance.leave;

public class AttendanceLeaveMapper {

    public static AttendanceLeaveResponse toResponse(AttendanceLeave entity) {
        if (entity == null) return null;

        return AttendanceLeaveResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .employeeId(entity.getEmployee().getId())
                .employeeName(entity.getEmployee().getFullName())
                .leaveDate(entity.getLeaveDate())
                .leaveType(entity.getLeaveType())
                .leaveReason(entity.getLeaveReason())
                .halfDay(entity.isHalfDay())
                .approved(entity.isApproved())
                .approvedBy(entity.getApprovedBy())
                .approvedDate(entity.getApprovedDate())
                .rejectionReason(entity.getRejectionReason())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

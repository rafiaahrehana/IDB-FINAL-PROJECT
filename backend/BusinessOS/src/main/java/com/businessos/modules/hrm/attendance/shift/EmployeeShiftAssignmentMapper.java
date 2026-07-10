package com.businessos.modules.hrm.attendance.shift;

public class EmployeeShiftAssignmentMapper {

    public static EmployeeShiftAssignmentResponse toResponse(EmployeeShiftAssignment entity) {
        if (entity == null) {
            return null;
        }

        return EmployeeShiftAssignmentResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null)
                .employeeName(entity.getEmployee() != null ? entity.getEmployee().getFullName() : null)
                .shiftId(entity.getShift() != null ? entity.getShift().getId() : null)
                .shiftName(entity.getShift() != null ? entity.getShift().getName() : null)
                .assignmentStartDate(entity.getAssignmentStartDate())
                .assignmentEndDate(entity.getAssignmentEndDate())
                .active(entity.isActive())
                .reason(entity.getReason())
                .assignedBy(entity.getAssignedBy())
                .notes(entity.getNotes())
                .build();
    }
}

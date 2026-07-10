package com.businessos.modules.itam.offboarding;

public class OffboardingChecklistMapper {

    public static OffboardingChecklistResponse toResponse(OffboardingChecklist entity) {
        if (entity == null) {
            return null;
        }

        return OffboardingChecklistResponse.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null)
                .employeeName(entity.getEmployee() != null ? entity.getEmployee().getFullName() : null)
                .offboardingDate(entity.getOffboardingDate())
                .targetCompletionDate(entity.getTargetCompletionDate())
                .hardwareCollected(entity.isHardwareCollected())
                .hardwareCollectedDate(entity.getHardwareCollectedDate())
                .licensesRevoked(entity.isLicensesRevoked())
                .licensesRevokedDate(entity.getLicensesRevokedDate())
                .accessRevoked(entity.isAccessRevoked())
                .accessRevokedDate(entity.getAccessRevokedDate())
                .dataHandedOver(entity.isDataHandedOver())
                .dataHandoverDate(entity.getDataHandoverDate())
                .exitInterviewCompleted(entity.isExitInterviewCompleted())
                .exitInterviewDate(entity.getExitInterviewDate())
                .completed(entity.isCompleted())
                .completionDate(entity.getCompletionDate())
                .completionPercentage(entity.getCompletionPercentage())
                .overallNotes(entity.getOverallNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

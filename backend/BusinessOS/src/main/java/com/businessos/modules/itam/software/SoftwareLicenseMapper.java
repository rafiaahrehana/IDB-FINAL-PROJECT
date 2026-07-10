package com.businessos.modules.itam.software;

public class SoftwareLicenseMapper {

    public static SoftwareLicenseResponse toResponse(SoftwareLicense entity) {
        if (entity == null) return null;

        return SoftwareLicenseResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .licenseKey(entity.getLicenseKey())
                .softwareName(entity.getSoftwareName())
                .publisher(entity.getPublisher())
                .licenseType(entity.getLicenseType())
                .totalSeatsLicensed(entity.getTotalSeatsLicensed())
                .seatsUsed(entity.getSeatsUsed())
                .seatsAvailable(entity.getSeatsAvailable())
                .licenseExpiryDate(entity.getLicenseExpiryDate())
                .licenseStatus(entity.getLicenseStatus())
                .renewalType(entity.getRenewalType())
                .nextRenewalDate(entity.getNextRenewalDate())
                .autoRenew(entity.isAutoRenew())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static SoftwareLicense toEntity(SoftwareLicenseRequest request) {
        if (request == null) return null;

        return SoftwareLicense.builder()
                .licenseKey(request.getLicenseKey())
                .softwareName(request.getSoftwareName())
                .publisher(request.getPublisher())
                .licenseType(request.getLicenseType())
                .totalSeatsLicensed(request.getTotalSeatsLicensed())
                .licenseExpiryDate(request.getLicenseExpiryDate())
                .renewalType(request.getRenewalType())
                .autoRenew(request.isAutoRenew())
                .notes(request.getNotes())
                .build();
    }
}
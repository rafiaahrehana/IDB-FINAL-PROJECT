package com.businessos.modules.itam.software;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SoftwareLicenseRequest {

    @NotBlank(message = "License key is required")
    private String licenseKey;

    @NotBlank(message = "Software name is required")
    private String softwareName;

    @NotBlank(message = "Publisher is required")
    private String publisher;

    private String version;

    @NotNull(message = "License type is required")
    private LicenseType licenseType;

    @Min(value = 1, message = "Total seats must be at least 1")
    private int totalSeatsLicensed;

    @NotNull(message = "License purchase date is required")
    private LocalDate licensePurchaseDate;

    @NotNull(message = "License cost is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal licenseCost;

    @NotNull(message = "License expiry date is required")
    private LocalDate licenseExpiryDate;

    @NotNull(message = "Renewal type is required")
    private LicenseRenewalType renewalType;

    private LocalDate nextRenewalDate;
    private BigDecimal renewalCost;

    private String vendor;
    private String accountEmail;
    private String licenseUrl;
    private String username;
    private String passwordHash;

    private String installationLocation;
    private int estimatedUserCount;
    private String complianceNotes;
    private String notes;
    private String renewalNotes;

    @Builder.Default
    private boolean autoRenew = true;
}
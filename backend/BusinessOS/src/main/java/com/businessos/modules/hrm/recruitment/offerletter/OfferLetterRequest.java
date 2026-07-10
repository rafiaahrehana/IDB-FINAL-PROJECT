package com.businessos.modules.hrm.recruitment.offerletter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OfferLetterRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    @NotNull(message = "Letter type is required")
    private LetterType letterType;
    @Size(max = 100)
    private String referenceNumber;
    @NotNull
    private LocalDate issueDate;
    @NotBlank(message = "Letter content is required")
    private String content;
    @Size(max = 150)
    private String signedBy;
}

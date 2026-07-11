package com.businessos.shared.address;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequest {

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Level 1 is required")
    private String level1;

    @NotBlank(message = "Level 2 is required")
    private String level2;

    @NotBlank(message = "Level 3 is required")
    private String level3;

    @NotBlank(message = "Level 4 is required")
    private String level4;

    @NotBlank(message = "Street Address is required")
    private String streetAddress;

    private String postalCode;
    private String apartment;
}

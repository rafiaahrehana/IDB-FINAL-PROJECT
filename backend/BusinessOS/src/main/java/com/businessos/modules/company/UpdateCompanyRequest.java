package com.businessos.modules.company;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCompanyRequest {
    @Size(min = 2, max = 150)
    private String companyName;
    @Size(max = 30)
    private String companyPhone;
    @Size(max = 255)
    private String website;
    @Size(max = 255)
    private String location;
    private String logo;
    @Size(max = 7)
    private String primaryColor;
    @Size(max = 7)
    private String secondaryColor;
    @Size(max = 255)
    private String tagline;
    private String portalAbout;
    private com.businessos.shared.address.LocationRequest locationDetail;
}

package com.businessos.modules.company;

import com.businessos.enums.CompanyStatus;
import com.businessos.enums.SubscriptionPlan;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CompanyResponse {
    private Long id;
    private String companyName;
    private String subdomain;
    private String companyEmail;
    private String companyPhone;
    private String website;
    private String location;
    private String logo;
    private String primaryColor;
    private String secondaryColor;
    private String tagline;
    private String portalAbout;
    private com.businessos.shared.address.LocationResponse locationDetail;
    private CompanyStatus status;
    private SubscriptionPlan subscriptionPlan;
    private LocalDate subscriptionStart;
    private LocalDate subscriptionEnd;
    private boolean trialExpired;
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private LocalDateTime createdAt;
}


package com.businessos.modules.crm.client;

import com.businessos.enums.ClientStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ClientResponse {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String image;
    private String clientCompanyName;
    private String industry;
    private String website;
    private String tags;
    private Integer employeeCount;
    private java.math.BigDecimal annualRevenue;
    private ClientStatus status;
    private boolean portalAccessEnabled;
    private Long accountManagerId;
    private String accountManagerName;
    private LocalDate onboardedAt;
    private LocalDateTime createdAt;
}
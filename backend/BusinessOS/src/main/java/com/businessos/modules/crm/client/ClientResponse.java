
package com.businessos.modules.crm.client;

import com.businessos.enums.ClientStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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
    private String taxId;
    private ClientStatus status;
    private boolean portalAccessEnabled;
    private Long accountManagerId;
    private String accountManagerName;
    private LocalDate onboardedAt;
    private LocalDateTime createdAt;

    // Account-level fields
    private String billingAddress;
    private String shippingAddress;
    private String tags;
    private Integer employeeCount;
    private BigDecimal annualRevenue;
    private BigDecimal lifetimeValue;
    private Integer totalRequests;
}

package com.businessos.modules.crm.lead;

import com.businessos.enums.LeadSource;
import com.businessos.enums.LeadStatus;
import com.businessos.enums.Priority;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeadResponse {
    
    private Long id;
    
    private String contactName;
    
    private String companyName;
    
    private String email;
    
    private String phone;
    
    private String industry;
    
    private String jobTitle;
    
    private String notes;
    
    private String description;
    
    private LeadStatus status;
    
    private LeadSource source;
    
    private Priority priority;
    
    private BigDecimal estimatedValue;
    
    private LocalDate expectedCloseDate;
    
    private LocalDate lastContactDate;
    
    private LocalDateTime lastActivityAt;
    
    private Long companyServiceId;
    
    private String companyServiceName;
    
    private Long assignedToId;
    
    private String assignedToName;
    
    private String assignedToEmail;
    
    private Long convertedClientId;
    
    private String convertedClientName;
    
    private boolean converted;
    
    private LocalDateTime convertedAt;
    
    private int activitiesCount;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String createdByName;
    
    private String updatedByName;
}

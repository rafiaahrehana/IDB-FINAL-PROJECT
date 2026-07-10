package com.businessos.modules.servicedesk.servicetemplate;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ServiceTemplateRequest {
    private String name;
    private String description;
    private Long categoryId;
    private BigDecimal defaultPrice;
    private Integer estimatedDays;
    private String iconUrl;
    private boolean active;
    
    private List<TemplateFormFieldRequest> formFields;
    private List<TemplateRequiredDocumentRequest> requiredDocuments;
    private List<TemplateWorkflowStageRequest> workflowStages;
}

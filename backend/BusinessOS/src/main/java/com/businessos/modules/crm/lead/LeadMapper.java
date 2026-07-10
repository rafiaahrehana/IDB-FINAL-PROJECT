package com.businessos.modules.crm.lead;

import com.businessos.auth.user.User;
import com.businessos.modules.crm.client.Client;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.servicedesk.companyservice.CompanyService;
import org.springframework.stereotype.Component;

@Component
public class LeadMapper {
    public LeadResponse toLeadResponse(Lead lead) {
        if (lead == null) {
            return null;
        }
        Employee assigned = lead.getAssignedTo();
        User assignedUser = assigned != null ? assigned.getUser() : null;
        CompanyService svc = lead.getInterestedService();
        Client client = lead.getConvertedClient();
        LeadResponse lr = new LeadResponse();
        lr.setId(lead.getId());
        lr.setContactName(lead.getContactName());
        lr.setCompanyName(lead.getCompanyName());
        lr.setEmail(lead.getEmail());
        lr.setPhone(lead.getPhone());
        lr.setIndustry(lead.getIndustry());
        lr.setNotes(lead.getNotes());
        lr.setStatus(lead.getStatus());
        lr.setEstimatedValue(lead.getEstimatedValue());
        lr.setConvertedAt(lead.getConvertedAt());
        lr.setAssignedToId(assigned != null ? assigned.getId() : null);
        lr.setAssignedToName(assignedUser != null ? assignedUser.getFullName() : null);
        lr.setCompanyServiceId(svc != null ? svc.getId() : null);
        lr.setCompanyServiceName(svc != null ? svc.getName() : null);
        lr.setConvertedClientId(client != null ? client.getId() : null);
        lr.setCreatedAt(lead.getCreatedAt());
        lr.setUpdatedAt(lead.getUpdatedAt());
        return lr;
    }

    public com.businessos.modules.crm.activity.CrmActivityResponse toActivityResponse(com.businessos.modules.crm.activity.CrmActivity activity) {
        if (activity == null) return null;
        com.businessos.modules.crm.activity.CrmActivityResponse res = new com.businessos.modules.crm.activity.CrmActivityResponse();
        res.setId(activity.getId());
        res.setType(activity.getType());
        res.setSubject(activity.getSubject());
        res.setDescription(activity.getDescription());
        res.setActivityDate(activity.getActivityDate());
        res.setScheduledAt(activity.getScheduledAt());
        res.setCompleted(activity.isCompleted());
        res.setSystemGenerated(activity.isSystemGenerated());
        res.setClientId(activity.getClient() != null ? activity.getClient().getId() : null);
        res.setOpportunityId(activity.getOpportunity() != null ? activity.getOpportunity().getId() : null);
        res.setPerformedById(activity.getPerformedBy() != null ? activity.getPerformedBy().getId() : null);
        res.setPerformedByName(activity.getPerformedBy() != null ? activity.getPerformedBy().getFullName() : null);
        res.setCreatedAt(activity.getCreatedAt());
        return res;
    }
}

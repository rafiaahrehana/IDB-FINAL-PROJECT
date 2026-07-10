package com.businessos.modules.crm.client;

import com.businessos.auth.user.User;
import com.businessos.modules.hrm.employee.Employee;

public class ClientMapper {
    public static ClientResponse toResponse(Client client) {
        User u = client.getUser();
        Employee employee = client.getAccountManager();
        ClientResponse response = new ClientResponse();
        response.setId(client.getId());
        response.setUserId(u != null ? u.getId():null);
        response.setFirstName(u != null ? u.getFirstName():null);
        response.setLastName(u != null ? u.getLastName():null);
        response.setEmail(u != null ? u.getEmail():null);
        response.setPhone(u != null ? u.getPhone():null);
        response.setImage(u != null ? u.getImage():null);
        response.setClientCompanyName(client.getClientCompanyName());
        response.setIndustry(client.getIndustry());
        response.setWebsite(client.getWebsite());
        response.setTags(client.getTags());
        response.setEmployeeCount(client.getEmployeeCount());
        response.setAnnualRevenue(client.getAnnualRevenue());
        response.setStatus(client.getStatus());
        response.setPortalAccessEnabled(client.isPortalAccessEnabled());
        response.setAccountManagerId(employee != null ? employee.getId() : null);
        response.setAccountManagerName(employee != null && employee.getUser() != null ? employee.getUser().getFullName() : null);
        response.setOnboardedAt(client.getOnboardedAt());
        response.setCreatedAt(client.getCreatedAt());
        return response;
    }
}

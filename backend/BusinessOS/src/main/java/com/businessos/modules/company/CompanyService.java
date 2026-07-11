package com.businessos.modules.company;

import com.businessos.enums.CompanyStatus;
import com.businessos.enums.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyService {
    CompanyPublicResponse getBySubdomain(String subdomain);

    java.util.List<com.businessos.modules.servicedesk.companyservice.CompanyServiceResponse> getPublicServices(String subdomain);
    CompanyResponse getById(Long id);
    CompanyResponse update(Long id, UpdateCompanyRequest request);
    CompanyResponse registerByAdmin(RegisterCompanyRequest request);
    Page<CompanyResponse> listAll(CompanyStatus status, SubscriptionPlan plan, String keyword, Pageable pageable);
    CompanyResponse changePlan(Long id, SubscriptionPlan plan, Double amountPaid, String transactionRef);
    CompanyResponse changeStatus(Long id, CompanyStatus status);
    void deactivate(Long id);
}

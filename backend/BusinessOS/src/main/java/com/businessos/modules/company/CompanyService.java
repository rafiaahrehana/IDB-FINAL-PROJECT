package com.businessos.modules.company;

import com.businessos.enums.CompanyStatus;
import com.businessos.core.subscription.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyService {
    CompanyPublicResponse getBySubdomain(String subdomain);
    CompanyResponse getById(Long id);
    CompanyResponse update(Long id, UpdateCompanyRequest request);
    CompanyResponse registerByAdmin(RegisterCompanyRequest request);
    Page<CompanyResponse> listAll(CompanyStatus status, Pageable pageable);
    CompanyResponse changePlan(Long id, SubscriptionPlan plan);
    CompanyResponse changeStatus(Long id, CompanyStatus status);
    void deactivate(Long id);
    void delete(Long id);
    void deactivateByOwner(Long companyId);
    void deleteByOwner(Long companyId);
    void suspendByAdmin(Long companyId);
    void activateByAdmin(Long companyId);
}

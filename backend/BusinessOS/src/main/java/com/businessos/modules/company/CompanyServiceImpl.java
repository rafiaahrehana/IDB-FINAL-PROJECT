package com.businessos.modules.company;

import com.businessos.auth.role.enums.Role;
import com.businessos.auth.user.User;
import com.businessos.auth.user.UserRepository;
import com.businessos.modules.crm.client.ClientRepository;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.enums.CompanyStatus;
import com.businessos.core.subscription.SubscriptionPlan;
import com.businessos.modules.servicedesk.servicerequest.ServiceRequestRepository;
import com.businessos.enums.ServiceRequestStatus;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor

@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public CompanyPublicResponse getBySubdomain(String subdomain) {
        Company company = companyRepository.findBySubdomain(subdomain)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with subdomain: " + subdomain));
        return CompanyMapper.toPublicResponse(company);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id : " + id));
        return CompanyMapper.toResponse(company);
    }

    @Override
    public CompanyResponse update(Long id, UpdateCompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (request.getCompanyName() != null) company.setCompanyName(request.getCompanyName());
        if (request.getCompanyPhone() != null) company.setCompanyPhone(request.getCompanyPhone());
        if (request.getWebsite() != null) company.setWebsite(request.getWebsite());
        if (request.getLocation() != null) company.setLocation(request.getLocation());
        if (request.getLogo() != null) company.setLogo(request.getLogo());
        if (request.getPrimaryColor() != null) company.setPrimaryColor(request.getPrimaryColor());
        if (request.getSecondaryColor() != null) company.setSecondaryColor(request.getSecondaryColor());
        if (request.getTagline() != null) company.setTagline(request.getTagline());

        company = companyRepository.save(company);
        return CompanyMapper.toResponse(company);
    }

    @Override
    public CompanyResponse registerByAdmin(RegisterCompanyRequest request) {
        if (companyRepository.existsBySubdomain(request.getSubdomain())) {
            throw new BadRequestException("Subdomain already exists.");
        }
        if (userRepository.existsByEmail(request.getOwnerEmail())) {
            throw new BadRequestException("Email already exists.");
        }

        User owner = new User();
        owner.setFirstName(request.getOwnerFirstName());
        owner.setLastName(request.getOwnerLastName());
        owner.setEmail(request.getOwnerEmail().toLowerCase().trim());
        owner.setPassword(passwordEncoder.encode(request.getOwnerPassword()));
        owner.setRole(Role.COMPANY_OWNER);
        owner.setPhone(request.getCompanyPhone());
        owner.setActive(true);
        owner = userRepository.save(owner);

        Company company = new Company();
        company.setCompanyName(request.getCompanyName());
        company.setSubdomain(request.getSubdomain());
        company.setCompanyPhone(request.getCompanyPhone());
        company.setCompanyEmail(request.getOwnerEmail().toLowerCase().trim());
        company.setOwner(owner);
        company.setStatus(CompanyStatus.ACTIVE);
        company.setSubscriptionPlan(SubscriptionPlan.FREE);
        company.setActive(true);
        company.setEmailVerified(true);

        company = companyRepository.save(company);
        return CompanyMapper.toResponse(company);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyResponse> listAll(CompanyStatus status, Pageable pageable) {
        if (status != null) {
            return companyRepository.findByStatus(status, pageable).map(CompanyMapper::toResponse);
        }
        return companyRepository.findAll(pageable).map(CompanyMapper::toResponse);
    }

    @Override
    public CompanyResponse changePlan(Long id, SubscriptionPlan plan) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        company.setSubscriptionPlan(plan);
        return CompanyMapper.toResponse(companyRepository.save(company));
    }

    @Override
    public CompanyResponse changeStatus(Long id, CompanyStatus status) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
                
        if (status == CompanyStatus.DEACTIVATED || status == CompanyStatus.SUSPENDED) {
            boolean hasActiveRequests = serviceRequestRepository.existsByCompanyIdAndStatusNotIn(
                id, List.of(ServiceRequestStatus.COMPLETED, ServiceRequestStatus.REJECTED, ServiceRequestStatus.CANCELLED)
            );
            if (hasActiveRequests) {
                throw new BadRequestException("Cannot change status. There are active service requests.");
            }
        }
        
        company.setStatus(status);
        if (status == CompanyStatus.ACTIVE) {
            company.setActive(true);
        } else if (status == CompanyStatus.SUSPENDED || status == CompanyStatus.DEACTIVATED) {
            company.setActive(false);
        }
        return CompanyMapper.toResponse(companyRepository.save(company));
    }

    @Override
    public void deactivate(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        boolean hasActiveRequests = serviceRequestRepository.existsByCompanyIdAndStatusNotIn(
            id, List.of(ServiceRequestStatus.COMPLETED, ServiceRequestStatus.REJECTED, ServiceRequestStatus.CANCELLED)
        );
        if (hasActiveRequests) {
            throw new BadRequestException("Cannot deactivate company. There are active service requests.");
        }

        company.setStatus(CompanyStatus.DEACTIVATED);
        company.setActive(false);
        companyRepository.save(company);
    }

    @Override
    public void delete(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        boolean hasActiveRequests = serviceRequestRepository.existsByCompanyIdAndStatusNotIn(
            id, List.of(ServiceRequestStatus.COMPLETED, ServiceRequestStatus.REJECTED, ServiceRequestStatus.CANCELLED)
        );
        if (hasActiveRequests) {
            throw new BadRequestException("Cannot delete company. There are active service requests.");
        }

        if (employeeRepository.existsByCompanyId(id)) {
            throw new BadRequestException("Cannot delete company with existing employees.");
        }

        if (clientRepository.existsByCompanyId(id)) {
            throw new BadRequestException("Cannot delete company with existing clients.");
        }

        company.setActive(false);
        company.setDeleted(true);
        company.setDeletedAt(LocalDateTime.now());
        companyRepository.save(company);
        
        if (company.getOwner() != null) {
            company.getOwner().setDeleted(true);
            company.getOwner().setDeletedAt(LocalDateTime.now());
            company.getOwner().setActive(false);
            userRepository.save(company.getOwner());
        }
    }

    @Override
    public void deactivateByOwner(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        boolean hasActiveRequests = serviceRequestRepository.existsByCompanyIdAndStatusNotIn(
            companyId, List.of(ServiceRequestStatus.COMPLETED, ServiceRequestStatus.REJECTED, ServiceRequestStatus.CANCELLED)
        );
        if (hasActiveRequests) {
            throw new BadRequestException("Cannot deactivate company. There are active service requests that must be completed or handed over first.");
        }

        company.setStatus(CompanyStatus.DEACTIVATED);
        company.setActive(false);
        companyRepository.save(company);
    }

    @Override
    public void deleteByOwner(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        boolean hasActiveRequests = serviceRequestRepository.existsByCompanyIdAndStatusNotIn(
            companyId, List.of(ServiceRequestStatus.COMPLETED, ServiceRequestStatus.REJECTED, ServiceRequestStatus.CANCELLED)
        );
        if (hasActiveRequests) {
            throw new BadRequestException("Cannot delete company. There are active service requests that must be completed or handed over first.");
        }

        if (employeeRepository.existsByCompanyId(companyId)) {
            throw new BadRequestException("Cannot delete company. All employees must be removed or handed over first.");
        }

        if (clientRepository.existsByCompanyId(companyId)) {
            throw new BadRequestException("Cannot delete company. All clients must be removed or transferred first.");
        }

        company.setActive(false);
        company.setDeleted(true);
        company.setDeletedAt(LocalDateTime.now());
        companyRepository.save(company);
        
        if (company.getOwner() != null) {
            company.getOwner().setDeleted(true);
            company.getOwner().setDeletedAt(LocalDateTime.now());
            company.getOwner().setActive(false);
            userRepository.save(company.getOwner());
        }
    }

    @Override
    public void suspendByAdmin(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        company.setStatus(CompanyStatus.SUSPENDED);
        company.setActive(false);
        companyRepository.save(company);
    }

    @Override
    public void activateByAdmin(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        company.setStatus(CompanyStatus.ACTIVE);
        company.setActive(true);
        companyRepository.save(company);
    }
}

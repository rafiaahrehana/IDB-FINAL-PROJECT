
package com.businessos.modules.crm.client;

import com.businessos.auth.role.enums.Role;
import com.businessos.modules.company.Company;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.auth.user.User;
import com.businessos.enums.ClientStatus;
import com.businessos.enums.CompanyStatus;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.auth.user.UserRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.email.EmailBranding;
import com.businessos.shared.email.EmailService;
import com.businessos.shared.notification.NotificationPreferenceService;
import com.businessos.modules.company.CompanyRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationPreferenceService notificationPreferenceService;
    private final SecurityUtil securityUtil;
    private final EmailService emailService;
    private final EmailBranding emailBranding;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public ClientResponse create(CreateClientRequest request) {
        Long companyId = requireCompanyId();

        String normalizedEmail = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.CLIENT)
                .active(true)
                .emailVerified(true)
                .build();
        userRepository.save(user);

        Client client = Client.builder()
                .user(user)
                .company(companyRef(companyId))   // was: full companyRepository.findById()
                .clientCompanyName(request.getClientCompanyName())
                .industry(request.getIndustry())
                .website(request.getWebsite())
                .taxId(request.getTaxId())
                .billingAddress(request.getBillingAddress())
                .shippingAddress(request.getShippingAddress())
                .tags(request.getTags())
                .employeeCount(request.getEmployeeCount())
                .annualRevenue(request.getAnnualRevenue())
                .status(ClientStatus.ACTIVE)
                .build();

        if (request.getAccountManagerId() != null) {
            Employee am = employeeRepository
                    .findByIdAndCompanyId(request.getAccountManagerId(), companyId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Account manager not found: " + request.getAccountManagerId()));
            client.setAccountManager(am);
        }

        clientRepository.save(client);
        notificationPreferenceService.createDefaultsForUser(user.getId());

        try {
            Company fullCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
            EmailBranding.Data branding = emailBranding.from(fullCompany);
            emailService.sendClientWelcomeEmail(user.getEmail(), user.getFirstName(), branding);
        } catch (Exception ex) {
            log.warn("Welcome email failed for client {}: {}", user.getEmail(), ex.getMessage());
        }

        
        return ClientMapper.toResponse(client);
    }

    @Override
    @Transactional
    public ClientResponse registerPublic(PublicClientRegisterRequest request) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + request.getCompanyId()));
        if (company.getStatus() != CompanyStatus.ACTIVE && company.getStatus() != CompanyStatus.TRIAL) {
            throw new BadRequestException("This company is not currently accepting client registrations");
        }

        String normalizedEmail = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.CLIENT)
                .active(true)
                .emailVerified(true)
                .build();
        userRepository.save(user);

        Client client = Client.builder()
                .user(user)
                .company(companyRef(company.getId()))
                .clientCompanyName(request.getClientCompanyName())
                .industry(request.getIndustry())
                .website(request.getWebsite())
                .status(ClientStatus.ACTIVE)
                .build();
        clientRepository.save(client);
        notificationPreferenceService.createDefaultsForUser(user.getId());

        try {
            EmailBranding.Data branding = emailBranding.from(company);
            emailService.sendClientWelcomeEmail(user.getEmail(), user.getFirstName(), branding);
        } catch (Exception ex) {
            log.warn("Welcome email failed for client {}: {}", user.getEmail(), ex.getMessage());
        }

        return ClientMapper.toResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getById(Long id) {
        return ClientMapper.toResponse(findInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getMyProfile() {
        User user = securityUtil.getCurrentUser();
        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));
        return ClientMapper.toResponse(client);
    }

    @Override
    @Transactional
    public ClientResponse updateMyProfile(UpdateMyClientProfileRequest request) {
        User user = securityUtil.getCurrentUser();
        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));

        if (request.getClientCompanyName() != null) client.setClientCompanyName(request.getClientCompanyName());
        if (request.getIndustry() != null) client.setIndustry(request.getIndustry());
        if (request.getWebsite() != null) client.setWebsite(request.getWebsite());
        if (request.getBillingAddress() != null) client.setBillingAddress(request.getBillingAddress());
        if (request.getShippingAddress() != null) client.setShippingAddress(request.getShippingAddress());

        clientRepository.save(client);
        return ClientMapper.toResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> listAll(ClientStatus status, Pageable pageable) {
        Long companyId = requireCompanyId();
        Page<Client> page = status != null
                ? clientRepository.findByCompanyIdAndStatus(companyId, status, pageable)
                : clientRepository.findByCompanyId(companyId, pageable);
        return page.map(ClientMapper::toResponse);
    }

    @Override
    @Transactional
    public ClientResponse update(Long id, UpdateClientRequest request) {
        Long companyId = requireCompanyId();
        Client client = findInTenant(id);

        if (request.getClientCompanyName()!= null) client.setClientCompanyName(request.getClientCompanyName());
        if (request.getIndustry()!= null) client.setIndustry(request.getIndustry());
        if (request.getWebsite()!= null) client.setWebsite(request.getWebsite());
        if (request.getTaxId()!= null) client.setTaxId(request.getTaxId());
        if (request.getStatus()!= null) client.setStatus(request.getStatus());
        if (request.getPortalAccessEnabled()!= null) client.setPortalAccessEnabled(request.getPortalAccessEnabled());
        if (request.getBillingAddress() != null) client.setBillingAddress(request.getBillingAddress());
        if (request.getShippingAddress() != null) client.setShippingAddress(request.getShippingAddress());
        if (request.getTags() != null) client.setTags(request.getTags());
        if (request.getEmployeeCount() != null) client.setEmployeeCount(request.getEmployeeCount());
        if (request.getAnnualRevenue() != null) client.setAnnualRevenue(request.getAnnualRevenue());

        if (request.getAccountManagerId() != null) {
            Employee am = employeeRepository
                    .findByIdAndCompanyId(request.getAccountManagerId(), companyId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Account manager not found: " + request.getAccountManagerId()));
            client.setAccountManager(am);
        }

        clientRepository.save(client);
        return ClientMapper.toResponse(client);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Client client = findInTenant(id);
        client.softDelete();
        clientRepository.save(client);

        User user = client.getUser();
        if (user != null) {
            user.setActive(false);
            user.softDelete();
            userRepository.save(user);
        }
        
    }

    @Override
    @Transactional(readOnly = true)
    public long getClientCount() {
        return clientRepository.countByCompanyId(requireCompanyId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isClient(Long userId) {
        return clientRepository.existsByUserIdAndCompanyId(userId, requireCompanyId());
    }

    private Client findInTenant(Long id) {
        return clientRepository.findByIdAndCompanyId(id, requireCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + id));
    }

    private Long requireCompanyId() {
        Long id = securityUtil.getCurrentCompanyId();
        if (id == null) throw new BadRequestException("No company context");
        return id;
    }

    private Company companyRef(Long companyId) {
        Company c = new Company();
        c.setId(companyId);
        return c;
    }
}
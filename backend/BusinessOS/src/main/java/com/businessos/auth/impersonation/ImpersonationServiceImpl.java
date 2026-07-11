package com.businessos.auth.impersonation;

import com.businessos.auth.role.enums.Role;
import com.businessos.auth.user.User;
import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.security.JwtService;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ImpersonationServiceImpl implements ImpersonationService {

    private final CompanyRepository companyRepository;
    private final ImpersonationAuditLogRepository impersonationAuditLogRepository;
    private final JwtService jwtService;
    private final SecurityUtil securityUtil;

    @Override
    public ImpersonationResponse startImpersonation(Long companyId, ImpersonateRequest request) {
        User admin = securityUtil.getCurrentUser();

        // Deliberately NOT checked against CompanyStatus - impersonation must work even for
        // a suspended/deactivated company, since that's exactly when platform staff need to
        // get in and fix the account. The suspension gate only blocks the company's own
        // users at normal login (see AuthServiceImpl.login).
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        String sessionId = UUID.randomUUID().toString();

        String accessToken = jwtService.generateImpersonationToken(
            admin.getEmail(), Role.COMPANY_OWNER.name(), companyId, admin.getId(), sessionId);

        impersonationAuditLogRepository.save(ImpersonationAuditLog.builder()
            .admin(admin)
            .company(company)
            .reason(request.getReason())
            .impersonationSessionId(sessionId)
            .startedAt(LocalDateTime.now())
            .build());

        return new ImpersonationResponse(
            accessToken,
            companyId,
            company.getCompanyName(),
            sessionId,
            jwtService.getImpersonationExpirationMs() / 1000);
    }

    @Override
    public void endImpersonation(EndImpersonationRequest request) {
        ImpersonationAuditLog log = impersonationAuditLogRepository
            .findByImpersonationSessionId(request.getImpersonationSessionId())
            .orElseThrow(() -> new ResourceNotFoundException("Impersonation session not found"));

        if (log.getEndedAt() == null) {
            log.setEndedAt(LocalDateTime.now());
            impersonationAuditLogRepository.save(log);
        }
    }
}

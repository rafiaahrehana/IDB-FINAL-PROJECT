package com.businessos.modules.support.audit;

import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportAuditServiceImpl implements SupportAuditService {

    private final SupportAuditLogRepository auditRepository;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional(readOnly = true)
    public SupportAuditLogResponse getById(Long id) {
        com.businessos.shared.audit.AuditLog log = auditRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found"));
        return SupportAuditLogMapper.toResponse(log);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportAuditLogResponse> getAll(Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return auditRepository.findByCompanyId(companyId, pageable)
                .map(SupportAuditLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportAuditLogResponse> getByActionType(String actionType, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        com.businessos.enums.AuditAction action = null;
        try {
            action = com.businessos.enums.AuditAction.valueOf(actionType);
        } catch (IllegalArgumentException e) {
            // If actionType doesn't map, return empty page or handle appropriately.
            // For now, let's just let it fail or we could return Page.empty()
            throw new BadRequestException("Invalid action type");
        }
        return auditRepository.findByCompanyIdAndAction(companyId, action, pageable)
                .map(SupportAuditLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportAuditLogResponse> getByResourceId(Long resourceId) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return auditRepository.findByCompanyIdAndEntityId(companyId, resourceId)
                .stream()
                .map(SupportAuditLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportAuditLogResponse> getByDateRange(LocalDate start, LocalDate end, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return auditRepository.findByCompanyIdAndPerformedAtBetween(companyId, start.atStartOfDay(), end.atStartOfDay(), pageable)
                .map(SupportAuditLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportAuditLogResponse> getByUser(Long userId, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return auditRepository.findByCompanyIdAndPerformedById(companyId, userId, pageable)
                .map(SupportAuditLogMapper::toResponse);
    }
}

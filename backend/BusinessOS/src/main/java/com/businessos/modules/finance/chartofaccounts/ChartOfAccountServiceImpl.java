package com.businessos.modules.finance.chartofaccounts;

import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartOfAccountServiceImpl implements ChartOfAccountService {

    private final ChartOfAccountRepository coaRepository;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public ChartOfAccountResponse create(ChartOfAccountRequest request) {
        Long companyId = securityUtil.getCurrentCompanyId();

        // Check if account code already exists
        if (coaRepository.findByCompanyIdAndAccountCode(companyId, request.getAccountCode()).isPresent()) {
            throw new BadRequestException("Account code already exists: " + request.getAccountCode());
        }

        ChartOfAccount account = ChartOfAccountMapper.toEntity(request);
        account.setCompanyId(companyId);
        account.setBalance(java.math.BigDecimal.ZERO);
        account = coaRepository.save(account);

        return ChartOfAccountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public ChartOfAccountResponse getById(Long id) {
        ChartOfAccount account = coaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chart of Account not found"));
        return ChartOfAccountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public ChartOfAccountResponse getByCode(String code) {
        Long companyId = securityUtil.getCurrentCompanyId();
        ChartOfAccount account = coaRepository.findByCompanyIdAndAccountCode(companyId, code)
                .orElseThrow(() -> new ResourceNotFoundException("Account code not found: " + code));
        return ChartOfAccountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChartOfAccountResponse> getAll(Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return coaRepository.findByCompanyId(companyId, pageable)
                .map(ChartOfAccountMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChartOfAccountResponse> getByType(AccountType type, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return coaRepository.findByCompanyIdAndTypeAndActiveTrue(companyId, type, pageable)
                .map(ChartOfAccountMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChartOfAccountResponse> getActiveAccounts() {
        Long companyId = securityUtil.getCurrentCompanyId();
        return coaRepository.findByCompanyIdAndActive(companyId, true)
                .stream()
                .map(ChartOfAccountMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChartOfAccountResponse update(Long id, ChartOfAccountRequest request) {
        ChartOfAccount account = coaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chart of Account not found"));

        // Validate code uniqueness if changed
        if (!account.getAccountCode().equals(request.getAccountCode())) {
            if (coaRepository.findByCompanyIdAndAccountCode(account.getCompanyId(), request.getAccountCode()).isPresent()) {
                throw new BadRequestException("Account code already exists: " + request.getAccountCode());
            }
            account.setAccountCode(request.getAccountCode());
        }

        account.setAccountName(request.getAccountName());
        account.setType(request.getType());
        account.setDescription(request.getDescription());
        account.setAllowDirectPosting(request.isAllowDirectPosting());
        account.setNotes(request.getNotes());

        account = coaRepository.save(account);
        return ChartOfAccountMapper.toResponse(account);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ChartOfAccount account = coaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chart of Account not found"));
        account.softDelete();
        coaRepository.save(account);
    }
}

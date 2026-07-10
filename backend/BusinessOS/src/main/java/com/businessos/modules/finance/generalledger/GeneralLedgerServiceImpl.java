package com.businessos.modules.finance.generalledger;

import com.businessos.modules.finance.chartofaccounts.ChartOfAccount;
import com.businessos.modules.finance.chartofaccounts.ChartOfAccountRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeneralLedgerServiceImpl implements GeneralLedgerService {

    private final GeneralLedgerRepository glRepository;
    private final ChartOfAccountRepository coaRepository;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public void recordTransaction(Long accountId, BigDecimal debitAmount, BigDecimal creditAmount,
                                  String description, String referenceType, Long referenceId,
                                  String referenceNumber) {
        Long companyId = securityUtil.getCurrentCompanyId();

        ChartOfAccount account = coaRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Chart of Account not found"));

        GeneralLedger entry = GeneralLedger.builder()
                .companyId(companyId)
                .transactionDate(LocalDate.now())
                .account(account)
                .debitAmount(debitAmount != null ? debitAmount : BigDecimal.ZERO)
                .creditAmount(creditAmount != null ? creditAmount : BigDecimal.ZERO)
                .description(description)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .referenceNumber(referenceNumber)
                .posted(true)
                .postedBy(securityUtil.getCurrentUser().getUsername())
                .postedDate(LocalDate.now())
                .build();

        glRepository.save(entry);

        // Update account balance
        updateAccountBalance(account, debitAmount, creditAmount);
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralLedgerResponse getById(Long id) {
        GeneralLedger entry = glRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GL entry not found"));
        return GeneralLedgerMapper.toResponse(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GeneralLedgerResponse> getAll(Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return glRepository.findByCompanyId(companyId, pageable)
                .map(GeneralLedgerMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GeneralLedgerResponse> getByAccount(Long accountId, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return glRepository.findByCompanyIdAndAccountId(companyId, accountId, pageable)
                .map(GeneralLedgerMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GeneralLedgerResponse> getByDateRange(LocalDate start, LocalDate end, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return glRepository.findByCompanyIdAndTransactionDateBetween(companyId, start, end, pageable)
                .map(GeneralLedgerMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeneralLedgerResponse> getByReference(String referenceType, Long referenceId) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return glRepository.findByCompanyIdAndReferenceTypeAndReferenceId(companyId, referenceType, referenceId)
                .stream()
                .map(GeneralLedgerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void reconcile(Long id, String notes) {
        GeneralLedger entry = glRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GL entry not found"));
        entry.setReconciled(true);
        entry.setReconciliationNotes(notes);
        glRepository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAccountBalance(Long accountId) {
        ChartOfAccount account = coaRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return account.getBalance();
    }

    private void updateAccountBalance(ChartOfAccount account, BigDecimal debitAmount, BigDecimal creditAmount) {
        BigDecimal currentBalance = account.getBalance();

        if (debitAmount != null && debitAmount.compareTo(BigDecimal.ZERO) > 0) {
            currentBalance = currentBalance.add(debitAmount);
        }
        if (creditAmount != null && creditAmount.compareTo(BigDecimal.ZERO) > 0) {
            currentBalance = currentBalance.subtract(creditAmount);
        }

        account.setBalance(currentBalance);
        coaRepository.save(account);
    }
}

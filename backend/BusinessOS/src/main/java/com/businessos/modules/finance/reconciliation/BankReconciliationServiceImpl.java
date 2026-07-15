package com.businessos.modules.finance.reconciliation;

import com.businessos.modules.finance.chartofaccounts.ChartOfAccount;
import com.businessos.modules.finance.chartofaccounts.ChartOfAccountRepository;
import com.businessos.modules.finance.generalledger.GeneralLedgerService;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
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

@Service
@RequiredArgsConstructor
public class BankReconciliationServiceImpl implements BankReconciliationService {

    private final BankReconciliationRepository reconciliationRepository;
    private final ChartOfAccountRepository coaRepository;
    private final GeneralLedgerService glService;
    private final SecurityUtil securityUtil;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional
    public BankReconciliationResponse create(BankReconciliationRequest request) {
        authorizationService.checkPermission(PermissionCode.BANK_RECONCILIATION_CREATE);
        Long companyId = securityUtil.getCurrentCompanyId();

        ChartOfAccount account = coaRepository.findByIdAndCompanyId(request.getBankAccountId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found"));

        BigDecimal glBalance = glService.getAccountBalance(account.getId());
        BigDecimal difference = glBalance.subtract(request.getBankStatementBalance());

        BankReconciliation reconciliation = BankReconciliation.builder()
                .companyId(companyId)
                .bankAccount(account)
                .reconciliationDate(LocalDate.now())
                .glBalance(glBalance)
                .bankStatementBalance(request.getBankStatementBalance())
                .difference(difference)
                .outstandingDeposits(request.getOutstandingDeposits())
                .outstandingChecks(request.getOutstandingChecks())
                .reconciled(false)
                .build();

        reconciliation = reconciliationRepository.save(reconciliation);
        return BankReconciliationMapper.toResponse(reconciliation);
    }

    @Override
    @Transactional(readOnly = true)
    public BankReconciliationResponse getById(Long id) {
        authorizationService.checkPermission(PermissionCode.BANK_RECONCILIATION_VIEW);
        return BankReconciliationMapper.toResponse(findInTenant(id));
    }

    private BankReconciliation findInTenant(Long id) {
        return reconciliationRepository.findByIdAndCompanyId(id, securityUtil.getCurrentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Reconciliation not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BankReconciliationResponse> getAll(Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.BANK_RECONCILIATION_VIEW);
        Long companyId = securityUtil.getCurrentCompanyId();
        return reconciliationRepository.findByCompanyId(companyId, pageable)
                .map(BankReconciliationMapper::toResponse);
    }

    @Override
    @Transactional
    public void markAsReconciled(Long id, String notes) {
        authorizationService.checkPermission(PermissionCode.BANK_RECONCILIATION_RECONCILE);
        BankReconciliation reconciliation = findInTenant(id);

        reconciliation.markAsReconciled(securityUtil.getCurrentUser().getUsername());
        reconciliation.setDiscrepancyNotes(notes);
        reconciliationRepository.save(reconciliation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankReconciliation> getPendingReconciliations() {
        authorizationService.checkPermission(PermissionCode.BANK_RECONCILIATION_VIEW);
        Long companyId = securityUtil.getCurrentCompanyId();
        return reconciliationRepository.findByCompanyIdAndReconciledFalse(companyId);
    }
}

package com.businessos.modules.finance.reports.service;

import com.businessos.modules.finance.chartofaccounts.ChartOfAccount;
import com.businessos.modules.finance.chartofaccounts.ChartOfAccountRepository;
import com.businessos.modules.finance.chartofaccounts.AccountType;
import com.businessos.modules.finance.generalledger.GeneralLedgerRepository;
import com.businessos.modules.finance.reports.dto.AccountLedger;
import com.businessos.modules.finance.reports.dto.BalanceSheetReport;
import com.businessos.modules.finance.reports.dto.ProfitLossReport;
import com.businessos.modules.finance.reports.dto.TrialBalanceReport;
import com.businessos.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialReportServiceImpl implements FinancialReportService {

    private final ChartOfAccountRepository coaRepository;
    private final GeneralLedgerRepository glRepository;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional(readOnly = true)
    public ProfitLossReport generateProfitLossReport(LocalDate startDate, LocalDate endDate) {
        Long companyId = securityUtil.getCurrentCompanyId();

        // Get revenue accounts
        List<ChartOfAccount> revenueAccounts = coaRepository
                .findByCompanyIdAndType(companyId, AccountType.REVENUE);
        BigDecimal totalRevenue = revenueAccounts.stream()
                .map(ChartOfAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get expense accounts
        List<ChartOfAccount> expenseAccounts = coaRepository
                .findByCompanyIdAndType(companyId, AccountType.EXPENSE);
        BigDecimal totalExpense = expenseAccounts.stream()
                .map(ChartOfAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netProfit = totalRevenue.subtract(totalExpense);

        return ProfitLossReport.builder()
                .periodStart(startDate)
                .periodEnd(endDate)
                .totalRevenue(totalRevenue)
                .totalExpense(totalExpense)
                .netProfit(netProfit)
                .generatedDate(LocalDate.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceSheetReport generateBalanceSheetReport(LocalDate asOfDate) {
        Long companyId = securityUtil.getCurrentCompanyId();

        // Assets
        List<ChartOfAccount> assetAccounts = coaRepository
                .findByCompanyIdAndType(companyId, AccountType.ASSET);
        BigDecimal totalAssets = assetAccounts.stream()
                .map(ChartOfAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Liabilities
        List<ChartOfAccount> liabilityAccounts = coaRepository
                .findByCompanyIdAndType(companyId, AccountType.LIABILITY);
        BigDecimal totalLiabilities = liabilityAccounts.stream()
                .map(ChartOfAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Equity
        List<ChartOfAccount> equityAccounts = coaRepository
                .findByCompanyIdAndType(companyId, AccountType.EQUITY);
        BigDecimal totalEquity = equityAccounts.stream()
                .map(ChartOfAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return BalanceSheetReport.builder()
                .asOfDate(asOfDate)
                .totalAssets(totalAssets)
                .totalLiabilities(totalLiabilities)
                .totalEquity(totalEquity)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TrialBalanceReport generateTrialBalanceReport(LocalDate asOfDate) {
        Long companyId = securityUtil.getCurrentCompanyId();

        List<ChartOfAccount> accounts = coaRepository.findByCompanyIdAndActive(companyId, true);

        List<TrialBalanceReport.AccountBalance> balances = accounts.stream()
                .map(acc -> TrialBalanceReport.AccountBalance.builder()
                        .accountId(acc.getId())
                        .accountCode(acc.getAccountCode())
                        .accountName(acc.getAccountName())
                        .debitBalance(acc.getDebitBalance())
                        .creditBalance(acc.getCreditBalance())
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalDebit = balances.stream()
                .map(TrialBalanceReport.AccountBalance::getDebitBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = balances.stream()
                .map(TrialBalanceReport.AccountBalance::getCreditBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return TrialBalanceReport.builder()
                .asOfDate(asOfDate)
                .accounts(balances)
                .totalDebit(totalDebit)
                .totalCredit(totalCredit)
                .generatedDate(LocalDate.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountLedger generateAccountLedger(Long accountId, LocalDate startDate, LocalDate endDate) {
        Long companyId = securityUtil.getCurrentCompanyId();

        ChartOfAccount account = coaRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        List<com.businessos.modules.finance.generalledger.GeneralLedgerResponse> transactions = glRepository
                .findTransactionsBetweenDates(companyId, startDate, endDate)
                .stream()
                .filter(gl -> gl.getAccount().getId().equals(accountId))
                .map(com.businessos.modules.finance.generalledger.GeneralLedgerMapper::toResponse)
                .collect(Collectors.toList());

        BigDecimal openingBalance = account.getBalance();

        return AccountLedger.builder()
                .accountCode(account.getAccountCode())
                .accountName(account.getAccountName())
                .periodStart(startDate)
                .periodEnd(endDate)
                .openingBalance(openingBalance)
                .entries(transactions)
                .closingBalance(account.getBalance())
                .generatedDate(LocalDate.now())
                .build();
    }
}



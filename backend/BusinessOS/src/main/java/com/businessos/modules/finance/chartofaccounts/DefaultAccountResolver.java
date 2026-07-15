package com.businessos.modules.finance.chartofaccounts;

import com.businessos.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Finds (or auto-creates) the standard Chart of Accounts entries that invoice/payment/
 * expense GL posting needs. Companies aren't seeded with a default chart of accounts,
 * so these are created on first use with fixed, well-known codes rather than requiring
 * manual setup before any transaction can post.
 */
@Component
@RequiredArgsConstructor
public class DefaultAccountResolver {

    private static final String CASH_CODE = "1000";
    private static final String CASH_NAME = "Cash and Bank";

    private static final String ACCOUNTS_RECEIVABLE_CODE = "1200";
    private static final String ACCOUNTS_RECEIVABLE_NAME = "Accounts Receivable";

    private static final String SALES_REVENUE_CODE = "4000";
    private static final String SALES_REVENUE_NAME = "Sales Revenue";

    private static final String TAX_PAYABLE_CODE = "2100";
    private static final String TAX_PAYABLE_NAME = "Tax Payable";

    private static final String OPERATING_EXPENSES_CODE = "5000";
    private static final String OPERATING_EXPENSES_NAME = "Operating Expenses";

    private static final String SALARY_EXPENSE_CODE = "5100";
    private static final String SALARY_EXPENSE_NAME = "Salaries and Wages";

    private static final String PAYROLL_PAYABLE_CODE = "2200";
    private static final String PAYROLL_PAYABLE_NAME = "Payroll Payable";

    private final ChartOfAccountRepository coaRepository;

    public ChartOfAccount cash(Long companyId) {
        return resolve(companyId, CASH_CODE, CASH_NAME, AccountType.ASSET);
    }

    public ChartOfAccount accountsReceivable(Long companyId) {
        return resolve(companyId, ACCOUNTS_RECEIVABLE_CODE, ACCOUNTS_RECEIVABLE_NAME, AccountType.ASSET);
    }

    public ChartOfAccount salesRevenue(Long companyId) {
        return resolve(companyId, SALES_REVENUE_CODE, SALES_REVENUE_NAME, AccountType.REVENUE);
    }

    public ChartOfAccount taxPayable(Long companyId) {
        return resolve(companyId, TAX_PAYABLE_CODE, TAX_PAYABLE_NAME, AccountType.LIABILITY);
    }

    public ChartOfAccount operatingExpenses(Long companyId) {
        return resolve(companyId, OPERATING_EXPENSES_CODE, OPERATING_EXPENSES_NAME, AccountType.EXPENSE);
    }

    public ChartOfAccount salaryExpense(Long companyId) {
        return resolve(companyId, SALARY_EXPENSE_CODE, SALARY_EXPENSE_NAME, AccountType.EXPENSE);
    }

    /** Withheld tax + other payroll deductions the company still owes out (not yet remitted). */
    public ChartOfAccount payrollPayable(Long companyId) {
        return resolve(companyId, PAYROLL_PAYABLE_CODE, PAYROLL_PAYABLE_NAME, AccountType.LIABILITY);
    }

    /**
     * If the company already has an account at this code (e.g. manually created before
     * ever posting a transaction), reuse it - but only if its type matches what GL
     * posting expects. A code reused with a mismatched type would silently misclassify
     * every debit/credit posted against it (wrong side of the balance sheet, wrong sign
     * in Trial Balance) with no error anywhere, so a mismatch fails loudly here instead.
     */
    @Transactional
    public ChartOfAccount resolve(Long companyId, String code, String name, AccountType type) {
        return coaRepository.findByCompanyIdAndAccountCode(companyId, code)
                .map(existing -> {
                    if (existing.getType() != type) {
                        throw new BadRequestException(
                                "Chart of Accounts code " + code + " already exists as \"" + existing.getAccountName()
                                        + "\" (" + existing.getType() + "), but " + name + " requires " + type
                                        + " - rename or recode the existing account before posting can continue.");
                    }
                    return existing;
                })
                .orElseGet(() -> coaRepository.save(ChartOfAccount.builder()
                        .companyId(companyId)
                        .accountCode(code)
                        .accountName(name)
                        .type(type)
                        .balance(BigDecimal.ZERO)
                        .active(true)
                        .allowDirectPosting(true)
                        .description("Auto-created default account")
                        .build()));
    }
}

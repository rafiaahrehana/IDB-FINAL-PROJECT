package com.businessos.modules.finance.expense;
import com.businessos.modules.finance.chartofaccounts.ChartOfAccount;
import com.businessos.modules.finance.chartofaccounts.DefaultAccountResolver;
import com.businessos.modules.finance.generalledger.GeneralLedgerService;
import com.businessos.modules.finance.generalledger.GlReferenceType;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import com.businessos.auth.user.User;
import com.businessos.auth.user.UserRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ForbiddenException;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service("financeExpenseService")
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final GeneralLedgerService glService;
    private final DefaultAccountResolver accountResolver;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        Long companyId = securityUtil.getCurrentCompanyId();
        User currentUser = securityUtil.getCurrentUser();
        if (currentUser == null) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        Long currentUserId = currentUser.getId();

        Employee employee = null;
        Long reqEmployeeId = request.getEmployeeId();
        if (reqEmployeeId != null) {
            employee = employeeRepository.findByIdAndCompanyId(reqEmployeeId, companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        } else if (currentUser.isTenantUser()) {
            employee = employeeRepository.findByUserId(currentUserId).orElse(null);
        }



        // Generate unique expense number
        String expenseNumber = generateExpenseNumber(companyId);

        Expense expense = Expense.builder()
                .companyId(companyId)
                .expenseNumber(expenseNumber)
                .title(request.getTitle() != null ? request.getTitle() : "Expense " + expenseNumber)
                .currency(request.getCurrency() != null ? request.getCurrency() : "BDT")
                .submittedBy(employee)
                .description(request.getDescription())
                .amount(request.getAmount())
                .vendorName(request.getVendorName())
                .category(request.getCategory())
                .expenseDate(request.getExpenseDate())
                .receiptUrl(request.getReceiptUrl())
                .status(ExpenseStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .notes(request.getNotes())
                .build();

        expense = expenseRepository.save(expense);
        return ExpenseMapper.toResponse(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getById(Long id) {
        Expense expense = findInTenant(id);
        if (!authorizationService.hasPermission(PermissionCode.EXPENSE_VIEW)) {
            requireOwnExpense(expense);
        }
        return ExpenseMapper.toResponse(expense);
    }

    private Expense findInTenant(Long id) {
        return expenseRepository.findByIdAndCompanyId(id, securityUtil.getCurrentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
    }

    private void requireOwnExpense(Expense expense) {
        User currentUser = securityUtil.getCurrentUser();
        Employee currentEmployee = currentUser != null
                ? employeeRepository.findByUserId(currentUser.getId()).orElse(null)
                : null;
        if (currentEmployee == null || expense.getSubmittedBy() == null
                || !expense.getSubmittedBy().getId().equals(currentEmployee.getId())) {
            throw new ForbiddenException("Access denied: you can only access your own expenses");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getAll(Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.EXPENSE_VIEW);
        Long companyId = securityUtil.getCurrentCompanyId();
        return expenseRepository.findByCompanyId(companyId, pageable)
                .map(ExpenseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getByStatus(ExpenseStatus status, Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.EXPENSE_VIEW);
        Long companyId = securityUtil.getCurrentCompanyId();
        return expenseRepository.findByCompanyIdAndStatus(companyId, status, pageable)
                .map(ExpenseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getByVendorName(String vendorName, Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.EXPENSE_VIEW);
        Long companyId = securityUtil.getCurrentCompanyId();
        return expenseRepository.findByCompanyIdAndVendorName(companyId, vendorName, pageable)
                .map(ExpenseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getMyExpenses(Long employeeId, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (employeeId == null) {
            User currentUser = securityUtil.getCurrentUser();
            if (currentUser == null) {
                throw new ResourceNotFoundException("User not authenticated");
            }
            employeeId = employeeRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"))
                    .getId();
        }
        return expenseRepository.findByCompanyIdAndSubmittedById(companyId, employeeId, pageable)
                .map(ExpenseMapper::toResponse);
    }

    @Override
    @Transactional
    public ExpenseResponse update(Long id, ExpenseRequest request) {
        Expense expense = findInTenant(id);

        if (!authorizationService.hasPermission(PermissionCode.EXPENSE_UPDATE)) {
            requireOwnExpense(expense);
        }

        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new BadRequestException("Can only update pending expenses");
        }

        if (request.getTitle() != null) expense.setTitle(request.getTitle());
        if (request.getCurrency() != null) expense.setCurrency(request.getCurrency());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setReceiptUrl(request.getReceiptUrl());
        expense.setNotes(request.getNotes());

        if (request.getVendorName() != null) {
            expense.setVendorName(request.getVendorName());
        }

        expense = expenseRepository.save(expense);
        return ExpenseMapper.toResponse(expense);
    }

    @Override
    @Transactional
    public void approveExpense(Long id, String approvalNotes) {
        authorizationService.checkPermission(PermissionCode.EXPENSE_APPROVE);
        Expense expense = findInTenant(id);

        User currentUser = securityUtil.getCurrentUser();
        if (currentUser == null) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        Long currentUserId = currentUser.getId();
        User approver = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        expense.approve(approver);
        expense.setApprovalNotes(approvalNotes);
        expenseRepository.save(expense);
    }

    @Override
    @Transactional
    public void rejectExpense(Long id, String reason) {
        authorizationService.checkPermission(PermissionCode.EXPENSE_REJECT);
        Expense expense = findInTenant(id);
        expense.reject();
        expense.setApprovalNotes(reason);
        expenseRepository.save(expense);
    }

    @Override
    @Transactional
    public void markAsPaid(Long id, String reimbursementMethod, String referenceNumber) {
        authorizationService.checkPermission(PermissionCode.EXPENSE_APPROVE);
        Expense expense = findInTenant(id);
        expense.markAsPaid(reimbursementMethod, referenceNumber);
        expenseRepository.save(expense);

        // Reimbursement previously only flipped the expense's own status - nothing
        // ever hit the ledger, so paid expenses were invisible to Finance reports.
        // Dr Operating Expenses / Cr Cash (category isn't a real CoA link today, so
        // it's recorded in the transaction description instead of a per-category account).
        Long companyId = expense.getCompanyId();
        String description = "Expense reimbursed: " + expense.getTitle()
                + (expense.getCategory() != null ? " (" + expense.getCategory() + ")" : "")
                + " - " + expense.getExpenseNumber();

        ChartOfAccount expenseAccount = accountResolver.operatingExpenses(companyId);
        glService.recordTransaction(expenseAccount.getId(), expense.getAmount(), BigDecimal.ZERO,
                description, GlReferenceType.EXPENSE, expense.getId(), expense.getExpenseNumber());

        ChartOfAccount cash = accountResolver.cash(companyId);
        glService.recordTransaction(cash.getId(), BigDecimal.ZERO, expense.getAmount(),
                description, GlReferenceType.EXPENSE, expense.getId(), expense.getExpenseNumber());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Expense expense = findInTenant(id);
        if (expense.getStatus() == ExpenseStatus.PAID) {
            throw new BadRequestException("Cannot delete a paid expense - it has GL entries");
        }
        expense.softDelete();
        expenseRepository.save(expense);
    }

    private String generateExpenseNumber(Long companyId) {
        int year = LocalDate.now().getYear();
        String prefix = "EXP-" + year + "-";
        String maxNumber = expenseRepository
                .findMaxExpenseNumberByCompanyAndPrefix(companyId, prefix)
                .orElse(prefix + "000000");
        long sequence = Long.parseLong(maxNumber.substring(prefix.length())) + 1;
        return String.format("%s%06d", prefix, sequence);
    }
}
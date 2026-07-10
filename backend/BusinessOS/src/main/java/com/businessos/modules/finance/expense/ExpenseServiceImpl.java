package com.businessos.modules.finance.expense;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.auth.user.User;
import com.businessos.auth.user.UserRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.email.EmailBranding;
import com.businessos.shared.email.EmailService;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service("financeExpenseService")
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final EmailService emailService;
    private final EmailBranding emailBranding;

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
            employee = employeeRepository.findById(reqEmployeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        } else if (currentUser.isTenantUser()) {
            employee = employeeRepository.findById(currentUserId).orElse(null);
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
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        return ExpenseMapper.toResponse(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getAll(Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return expenseRepository.findByCompanyId(companyId, pageable)
                .map(ExpenseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getByStatus(ExpenseStatus status, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return expenseRepository.findByCompanyIdAndStatus(companyId, status, pageable)
                .map(ExpenseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getByVendorName(String vendorName, Pageable pageable) {
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
            employeeId = currentUser.getId();
        }
        return expenseRepository.findByCompanyIdAndSubmittedByIdAndStatus(companyId, employeeId, ExpenseStatus.PENDING, pageable)
                .map(ExpenseMapper::toResponse);
    }

    @Override
    @Transactional
    public ExpenseResponse update(Long id, ExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new RuntimeException("Can only update pending expenses");
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
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

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

        // Send expense approval email to submitter
        if (expense.getSubmittedBy() != null && expense.getSubmittedBy().getUser() != null) {
            String to = expense.getSubmittedBy().getUser().getEmail();
            String name = expense.getSubmittedBy().getUser().getFullName();
            EmailBranding.Data branding = emailBranding.from(expense.getSubmittedBy() != null ? expense.getSubmittedBy().getCompany() : null);
            emailService.sendExpenseStatusEmail(to, name, expense.getTitle(), "APPROVED", branding);
        }
    }

    @Override
    @Transactional
    public void rejectExpense(Long id, String reason) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        expense.reject();
        expense.setApprovalNotes(reason);
        expenseRepository.save(expense);

        // Send expense rejection email to submitter
        if (expense.getSubmittedBy() != null && expense.getSubmittedBy().getUser() != null) {
            String to = expense.getSubmittedBy().getUser().getEmail();
            String name = expense.getSubmittedBy().getUser().getFullName();
            EmailBranding.Data branding = emailBranding.from(expense.getSubmittedBy() != null ? expense.getSubmittedBy().getCompany() : null);
            emailService.sendExpenseStatusEmail(to, name, expense.getTitle(), "REJECTED", branding);
        }
    }

    @Override
    @Transactional
    public void markAsPaid(Long id, String reimbursementMethod, String referenceNumber) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        expense.markAsPaid(reimbursementMethod, referenceNumber);
        expenseRepository.save(expense);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        expense.softDelete();
        expenseRepository.save(expense);
    }

    private String generateExpenseNumber(Long companyId) {
        long count = expenseRepository.count() + 1;
        return String.format("EXP-%04d-%06d", LocalDate.now().getYear(), count);
    }
}
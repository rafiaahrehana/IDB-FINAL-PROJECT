package com.businessos.modules.hrm.payroll;

import com.businessos.modules.company.Company;
import com.businessos.modules.finance.chartofaccounts.ChartOfAccount;
import com.businessos.modules.finance.chartofaccounts.DefaultAccountResolver;
import com.businessos.modules.finance.generalledger.GeneralLedgerService;
import com.businessos.modules.finance.generalledger.GlReferenceType;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.salary.SalaryStructure;
import com.businessos.modules.hrm.salary.SalaryStructureRepository;
import com.businessos.enums.PayrollStatus;
import com.businessos.enums.PaymentMethod;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.shared.email.EmailBranding;
import com.businessos.shared.email.EmailService;
import com.businessos.modules.company.CompanyRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final SalaryStructureRepository salaryStructureRepository;
    private final SecurityUtil securityUtil;
    private final EmailService emailService;
    private final EmailBranding emailBranding;
    private final GeneralLedgerService glService;
    private final DefaultAccountResolver accountResolver;

    @Override
    @Transactional
    public PayrollResponse create(CreatePayrollRequest request) {
        Long companyId = requireCompanyId();

        if (payrollRepository.findByEmployeeIdAndPayMonthAndPayYear(
                request.getEmployeeId(), request.getPayMonth(), request.getPayYear()).isPresent()) {
            throw new BadRequestException("Payroll already exists for this employee and period");
        }

        Employee employee = employeeRepository.findByIdAndCompanyId(request.getEmployeeId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + request.getEmployeeId()));

        SalaryComponents comps = resolveSalaryComponents(employee, request.getPayMonth(), request.getPayYear(),
                request.getBasicSalary(), request.getHouseRent(), request.getMedicalAllowance(), request.getTransportAllowance(),
                request.getFoodAllowance(), request.getSpecialAllowance());

        BigDecimal bonus = orZero(request.getBonus());
        BigDecimal deductions = orZero(request.getDeductions());
        BigDecimal tax = orZero(request.getTaxDeduction());
        BigDecimal insurance = orZero(request.getInsuranceDeduction());
        BigDecimal providentFund = orZero(request.getProvidentFundDeduction());
        BigDecimal gross = comps.basic().add(comps.rent()).add(comps.medical()).add(comps.transport())
                .add(comps.food()).add(comps.special()).add(bonus);
        BigDecimal net = gross.subtract(deductions).subtract(tax).subtract(insurance).subtract(providentFund);

        Payroll payroll = Payroll.builder()
                .employee(employee)
                .company(companyRef(companyId))
                .payMonth(request.getPayMonth())
                .payYear(request.getPayYear())
                .basicSalary(comps.basic())
                .houseRent(comps.rent())
                .medicalAllowance(comps.medical())
                .transportAllowance(comps.transport())
                .foodAllowance(comps.food())
                .specialAllowance(comps.special())
                .bonus(bonus)
                .deductions(deductions)
                .taxDeduction(tax)
                .insuranceDeduction(insurance)
                .providentFundDeduction(providentFund)
                .netSalary(net)
                .notes(request.getNotes())
                .status(PayrollStatus.DRAFT)
                .build();

        payrollRepository.save(payroll);

        return PayrollMapper.toPayrollResponse(payroll);
    }

    @Override
    @Transactional
    public BulkPayrollResult generateForAllEmployees(int month, int year) {
        Long companyId = requireCompanyId();
        List<Employee> employees = employeeRepository.findByCompanyIdAndActiveTrue(companyId);

        List<String> created = new ArrayList<>();
        List<String> skippedAlreadyExists = new ArrayList<>();
        List<String> skippedNoStructure = new ArrayList<>();

        for (Employee employee : employees) {
            String name = employeeDisplayName(employee);

            if (payrollRepository.findByEmployeeIdAndPayMonthAndPayYear(employee.getId(), month, year).isPresent()) {
                skippedAlreadyExists.add(name);
                continue;
            }

            Optional<SalaryStructure> structureOpt = activeStructure(employee.getId(), month, year);
            if (structureOpt.isEmpty()) {
                skippedNoStructure.add(name);
                continue;
            }
            SalaryStructure structure = structureOpt.get();
            SalaryComponents comps = fromStructure(structure);
            BigDecimal tax = orZero(structure.getTaxDeduction());
            BigDecimal providentFund = orZero(structure.getProvidentFund());
            BigDecimal gross = comps.basic().add(comps.rent()).add(comps.medical()).add(comps.transport())
                    .add(comps.food()).add(comps.special());
            BigDecimal net = gross.subtract(providentFund).subtract(tax);

            Payroll payroll = Payroll.builder()
                    .employee(employee)
                    .company(companyRef(companyId))
                    .payMonth(month)
                    .payYear(year)
                    .basicSalary(comps.basic())
                    .houseRent(comps.rent())
                    .medicalAllowance(comps.medical())
                    .transportAllowance(comps.transport())
                    .foodAllowance(comps.food())
                    .specialAllowance(comps.special())
                    .bonus(BigDecimal.ZERO)
                    .taxDeduction(tax)
                    .providentFundDeduction(providentFund)
                    .netSalary(net)
                    .status(PayrollStatus.DRAFT)
                    .build();
            payrollRepository.save(payroll);
            created.add(name);
        }

        return BulkPayrollResult.builder()
                .created(created)
                .skippedAlreadyExists(skippedAlreadyExists)
                .skippedNoSalaryStructure(skippedNoStructure)
                .build();
    }

    private record SalaryComponents(BigDecimal basic, BigDecimal rent, BigDecimal medical, BigDecimal transport,
            BigDecimal food, BigDecimal special) {}

    private Optional<SalaryStructure> activeStructure(Long employeeId, int payMonth, int payYear) {
        return salaryStructureRepository.findActiveForEmployeeOnDate(employeeId, LocalDate.of(payYear, payMonth, 1));
    }

    private SalaryComponents fromStructure(SalaryStructure s) {
        return new SalaryComponents(orZero(s.getBasicSalary()), orZero(s.getHouseRent()),
                orZero(s.getMedicalAllowance()), orZero(s.getTransportAllowance()),
                orZero(s.getFoodAllowance()), orZero(s.getSpecialAllowance()));
    }

    /**
     * If basicSalary was provided manually, use the request's numbers as-is (matches
     * the previous behavior exactly). Otherwise pull from the employee's salary
     * structure active during this pay period - previously this lookup existed
     * (findActiveForEmployeeOnDate) but nothing ever called it, so HR had to hand-type
     * every salary component for every employee every month.
     */
    private SalaryComponents resolveSalaryComponents(Employee employee, int payMonth, int payYear,
            BigDecimal manualBasic, BigDecimal manualRent, BigDecimal manualMedical, BigDecimal manualTransport,
            BigDecimal manualFood, BigDecimal manualSpecial) {
        if (manualBasic != null) {
            return new SalaryComponents(manualBasic, orZero(manualRent), orZero(manualMedical), orZero(manualTransport),
                    orZero(manualFood), orZero(manualSpecial));
        }
        SalaryStructure structure = activeStructure(employee.getId(), payMonth, payYear)
                .orElseThrow(() -> new BadRequestException(
                        "No active salary structure for " + employeeDisplayName(employee) + " for " + payMonth + "/" + payYear
                                + " - set one up under Salary Structures, or provide basicSalary manually"));
        return fromStructure(structure);
    }

    private String employeeDisplayName(Employee employee) {
        return employee.getUser() != null ? employee.getUser().getFullName() : "Employee #" + employee.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollResponse getById(Long id) {
        return PayrollMapper.toPayrollResponse(findInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayrollResponse> listByPeriod(int month, int year, Pageable pageable) {
        return payrollRepository.findByCompanyIdAndPayMonthAndPayYear(
                requireCompanyId(), month, year, pageable)
                .map(PayrollMapper::toPayrollResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayrollResponse> listForEmployee(Long employeeId, Pageable pageable) {
        return payrollRepository.findByCompanyIdAndEmployeeId(requireCompanyId(), employeeId, pageable)
                .map(PayrollMapper::toPayrollResponse);
    }

    @Override
    @Transactional
    public PayrollResponse approve(Long id) {
        Payroll p = findInTenant(id);
        if (p.getStatus() != PayrollStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT payrolls can be approved");
        }
        Employee approver = employeeRepository.findByUserId(securityUtil.getCurrentUser().getId())
                .orElseThrow(() -> new BadRequestException("Employee profile not found"));
        p.setStatus(PayrollStatus.APPROVED);
        p.setApprovedBy(approver);
        payrollRepository.save(p); // explicit save for clarity and transaction safety
        return PayrollMapper.toPayrollResponse(p);
    }

    @Override
    @Transactional
    public PayrollResponse markPaid(Long id, String paymentReference, PaymentMethod paymentMethod) {
        Payroll p = findInTenant(id);
        if (p.getStatus() != PayrollStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED payrolls can be marked as paid");
        }
        p.setStatus(PayrollStatus.PAID);
        p.setPaymentReference(paymentReference);
        p.setPaymentMethod(paymentMethod);
        p.setPaidAt(LocalDate.now());

        postPayrollToLedger(p);

        if (p.getEmployee().getUser() != null) {
            try {
                EmailBranding.Data branding = emailBranding.from(p.getCompany());
                emailService.sendPayrollEmail(
                        p.getEmployee().getUser().getEmail(),
                        p.getEmployee().getUser().getFirstName(), branding);
            } catch (Exception ex) {
                log.warn("Payroll email failed for employee {}: {}", p.getEmployee().getUser().getEmail(), ex.getMessage());
            }
        }

        return PayrollMapper.toPayrollResponse(p);
    }

    /**
     * Payroll disbursement previously never touched the ledger at all - salary,
     * often a company's single biggest expense, was completely invisible to
     * Finance reports. Dr Salaries and Wages (gross) / Cr Cash (net paid out) /
     * Cr Payroll Payable (tax + deductions withheld but not yet remitted).
     */
    private void postPayrollToLedger(Payroll p) {
        Long companyId = p.getCompany().getId();
        BigDecimal gross = p.getBasicSalary().add(p.getHouseRent()).add(p.getMedicalAllowance())
                .add(p.getTransportAllowance()).add(p.getFoodAllowance()).add(p.getSpecialAllowance()).add(p.getBonus());
        BigDecimal withheld = p.getDeductions().add(p.getTaxDeduction())
                .add(p.getInsuranceDeduction()).add(p.getProvidentFundDeduction());
        String description = "Payroll " + p.getPayMonth() + "/" + p.getPayYear()
                + " for " + p.getEmployee().getUser().getFullName();

        ChartOfAccount salaryExpense = accountResolver.salaryExpense(companyId);
        glService.recordTransaction(salaryExpense.getId(), gross, BigDecimal.ZERO,
                description, GlReferenceType.PAYROLL, p.getId(), p.getPaymentReference());

        ChartOfAccount cash = accountResolver.cash(companyId);
        glService.recordTransaction(cash.getId(), BigDecimal.ZERO, p.getNetSalary(),
                description, GlReferenceType.PAYROLL, p.getId(), p.getPaymentReference());

        if (withheld.compareTo(BigDecimal.ZERO) > 0) {
            ChartOfAccount payable = accountResolver.payrollPayable(companyId);
            glService.recordTransaction(payable.getId(), BigDecimal.ZERO, withheld,
                    description, GlReferenceType.PAYROLL, p.getId(), p.getPaymentReference());
        }

        p.setGlDebitAccount(salaryExpense.getAccountCode());
        p.setGlCreditAccount(cash.getAccountCode());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Payroll p = findInTenant(id);
        if (p.getStatus() == PayrollStatus.PAID) {
            throw new BadRequestException("Cannot delete a paid payroll");
        }
        p.softDelete();
    }

    private Payroll findInTenant(Long id) {
        return payrollRepository.findByIdAndCompanyId(id, requireCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found: " + id));
    }

    private Long requireCompanyId() {
        Long id = securityUtil.getCurrentCompanyId();
        if (id == null)
            throw new BadRequestException("No company context");
        return id;
    }

    private Company companyRef(Long companyId) {
        Company c = new Company();
        c.setId(companyId);
        return c;
    }

    private BigDecimal orZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}

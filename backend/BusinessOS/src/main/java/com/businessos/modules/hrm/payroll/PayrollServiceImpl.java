package com.businessos.modules.hrm.payroll;

import com.businessos.modules.company.Company;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.enums.PayrollStatus;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final SecurityUtil securityUtil;
    private final EmailService emailService;
    private final EmailBranding emailBranding;

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

        BigDecimal basic = request.getBasicSalary();
        BigDecimal rent = orZero(request.getHouseRent());
        BigDecimal medical = orZero(request.getMedicalAllowance());
        BigDecimal transport = orZero(request.getTransportAllowance());
        BigDecimal bonus = orZero(request.getBonus());
        BigDecimal deductions = orZero(request.getDeductions());
        BigDecimal tax = orZero(request.getTaxDeduction());
        BigDecimal gross = basic.add(rent).add(medical).add(transport).add(bonus);
        BigDecimal net = gross.subtract(deductions).subtract(tax);

        Payroll payroll = Payroll.builder()
                .employee(employee)
                .company(companyRef(companyId))
                .payMonth(request.getPayMonth())
                .payYear(request.getPayYear())
                .basicSalary(basic)
                .houseRent(rent)
                .medicalAllowance(medical)
                .transportAllowance(transport)
                .bonus(bonus)
                .deductions(deductions)
                .taxDeduction(tax)
                .netSalary(net)
                .notes(request.getNotes())
                .status(PayrollStatus.DRAFT)
                .build();

        payrollRepository.save(payroll);

        return PayrollMapper.toPayrollResponse(payroll);
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
    public PayrollResponse markPaid(Long id, String paymentReference) {
        Payroll p = findInTenant(id);
        if (p.getStatus() != PayrollStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED payrolls can be marked as paid");
        }
        p.setStatus(PayrollStatus.PAID);
        p.setPaymentReference(paymentReference);
        p.setPaidAt(LocalDate.now());

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

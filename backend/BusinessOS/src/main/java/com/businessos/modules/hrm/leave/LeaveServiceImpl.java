package com.businessos.modules.hrm.leave;

import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.modules.hrm.leave.leavebalance.LeaveBalanceMapper;
import com.businessos.modules.hrm.leave.leavebalance.LeaveBalanceRepository;
import com.businessos.modules.hrm.leave.leavebalance.LeaveBalanceResponse;
import com.businessos.modules.hrm.leave.leaverequest.*;
import com.businessos.modules.company.Company;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.auth.user.User;
import com.businessos.enums.LeaveRequestStatus;
import java.util.List;

import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.shared.email.EmailBranding;
import com.businessos.shared.email.EmailService;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final SecurityUtil securityUtil;
    private final EmailService emailService;
    private final EmailBranding emailBranding;

    @Override
    @Transactional
    public LeaveRequestResponse apply(LeaveRequestDto request) {
        Long companyId = requireCompanyId();
        User currentUser = securityUtil.getCurrentUser();
        Employee employee = employeeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new BadRequestException("Employee profile not found"));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be on or after start date");
        }

        if (leaveRequestRepository.hasOverlappingLeave(
                employee.getId(), request.getStartDate(), request.getEndDate(),
                List.of(LeaveRequestStatus.REJECTED, LeaveRequestStatus.CANCELLED))) {
            throw new BadRequestException("You already have a leave requeststatus overlapping this period");
        }

        int totalDays = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        // Check leave balance
        leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(
                employee.getId(), request.getLeaveType(), request.getStartDate().getYear())
                .ifPresent(balance -> {
                    if (balance.getRemainingDays() < totalDays) {
                        throw new BadRequestException("Insufficient " + request.getLeaveType()
                                + " leave balance. Available: " + balance.getRemainingDays()
                                + " days, Requested: " + totalDays + " days.");
                    }
                    balance.setPendingDays(balance.getPendingDays() + totalDays);
                });

        LeaveRequest lr = LeaveRequest.builder()
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(totalDays)
                .reason(request.getReason())
                .status(LeaveRequestStatus.PENDING)
                .employee(employee)
                .company(companyRef(companyId))
                .build();

        leaveRequestRepository.save(lr);
        return LeaveRequestMapper.toLeaveRequestResponse(lr);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveRequestResponse getById(Long id) {
        return LeaveRequestMapper.toLeaveRequestResponse(findInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveRequestResponse> listAll(LeaveRequestStatus status, Pageable pageable) {
        Long companyId = requireCompanyId();
        Page<LeaveRequest> page = status != null
                ? leaveRequestRepository.findByCompanyIdAndStatus(companyId, status, pageable)
                : leaveRequestRepository.findByCompanyId(companyId, pageable);
        return page.map(LeaveRequestMapper::toLeaveRequestResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveRequestResponse> listMyLeaves(Pageable pageable) {
        Long companyId = requireCompanyId();
        Employee emp = employeeRepository.findByUserId(securityUtil.getCurrentUser().getId())
                .orElseThrow(() -> new BadRequestException("Employee profile not found"));
        return leaveRequestRepository.findByCompanyIdAndEmployeeId(companyId, emp.getId(), pageable)
                .map(LeaveRequestMapper::toLeaveRequestResponse);
    }

    @Override
    @Transactional
    public LeaveRequestResponse review(Long id, ReviewLeaveRequest request) {
        LeaveRequest lr = findInTenant(id);
        if (lr.getStatus() != LeaveRequestStatus.PENDING) {
            throw new BadRequestException("Only PENDING leave requests can be reviewed");
        }
        if (request.getStatus() == LeaveRequestStatus.REJECTED
                && (request.getRejectionReason() == null || request.getRejectionReason().isBlank())) {
            throw new BadRequestException("Rejection reason is required when rejecting a leave requeststatus");
        }

        User reviewer = securityUtil.getCurrentUser();

        lr.setStatus(request.getStatus());
        lr.setRejectionReason(request.getRejectionReason());
        lr.setReviewedBy(reviewer);
        lr.setReviewedAt(LocalDateTime.now());

        // Update leave balance
        leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(
                lr.getEmployee().getId(), lr.getLeaveType(), lr.getStartDate().getYear())
                .ifPresent(balance -> {
                    balance.setPendingDays(Math.max(0, balance.getPendingDays() - lr.getTotalDays()));
                    if (request.getStatus() == LeaveRequestStatus.APPROVED) {
                        balance.setUsedDays(balance.getUsedDays() + lr.getTotalDays());
                    }
                });

        if (request.getStatus() == LeaveRequestStatus.APPROVED && lr.getEmployee().getUser() != null) {
            try {
                EmailBranding.Data branding = emailBranding.from(lr.getCompany());
                emailService.sendLeaveApprovalEmail(
                        lr.getEmployee().getUser().getEmail(),
                        lr.getEmployee().getUser().getFirstName(), branding);
            } catch (Exception ex) {
                log.warn("Leave approval email failed for employee {}: {}", lr.getEmployee().getUser().getEmail(), ex.getMessage());
            }
        }

        return LeaveRequestMapper.toLeaveRequestResponse(lr);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        LeaveRequest lr = findInTenant(id);
        if (lr.getStatus() == LeaveRequestStatus.APPROVED
                && lr.getStartDate().isBefore(java.time.LocalDate.now())) {
            throw new BadRequestException("Cannot cancel a leave that has already started");
        }
        leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(
                lr.getEmployee().getId(), lr.getLeaveType(), lr.getStartDate().getYear())
                .ifPresent(balance -> {
                    if (lr.getStatus() == LeaveRequestStatus.PENDING) {
                        balance.setPendingDays(Math.max(0, balance.getPendingDays() - lr.getTotalDays()));
                    } else if (lr.getStatus() == LeaveRequestStatus.APPROVED) {
                        balance.setUsedDays(Math.max(0, balance.getUsedDays() - lr.getTotalDays()));
                    }
                });
        lr.setStatus(LeaveRequestStatus.CANCELLED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getMyBalances(int year) {
        Employee emp = employeeRepository.findByUserId(securityUtil.getCurrentUser().getId())
                .orElseThrow(() -> new BadRequestException("Employee profile not found"));
        return leaveBalanceRepository.findByEmployeeIdAndYear(emp.getId(), year)
                .stream().map(LeaveBalanceMapper::toLeaveBalanceResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getBalancesForEmployee(Long employeeId, int year) {
        Long companyId = requireCompanyId();
        Employee emp = employeeRepository.findByIdAndCompanyId(employeeId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return leaveBalanceRepository.findByEmployeeIdAndYear(emp.getId(), year)
                .stream().map(LeaveBalanceMapper::toLeaveBalanceResponse).toList();
    }

    private LeaveRequest findInTenant(Long id) {
        return leaveRequestRepository.findByIdAndCompanyId(id, requireCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave requeststatus not found: " + id));
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
}

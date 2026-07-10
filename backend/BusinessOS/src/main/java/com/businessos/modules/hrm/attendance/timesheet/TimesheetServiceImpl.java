package com.businessos.modules.hrm.attendance.timesheet;

import com.businessos.modules.company.Company;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.servicedesk.task.Task;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.modules.servicedesk.task.TaskRepository;
import com.businessos.security.SecurityUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor

public class TimesheetServiceImpl implements TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final EmployeeRepository  employeeRepository;
    private final TaskRepository      taskRepository;
    private final SecurityUtil        securityUtil;

    @Override
    @Transactional
    public TimesheetResponse log(TimesheetRequest request) {
        Long companyId = requireCompanyId();
        Employee employee = employeeRepository.findByUserId(securityUtil.getCurrentUser().getId())
            .orElseThrow(() -> new BadRequestException("Employee profile not found"));

        if (timesheetRepository.findByEmployeeIdAndWorkDate(employee.getId(), request.getWorkDate()).isPresent()) {
            throw new BadRequestException("Timesheet already logged for " + request.getWorkDate());
        }

        Timesheet ts = new Timesheet(); ts.setEmployee(employee); ts.setCompany(companyRef(companyId)); ts.setWorkDate(request.getWorkDate()); ts.setStartTime(request.getStartTime() != null ? request.getStartTime().toLocalTime() : null); ts.setEndTime(request.getEndTime() != null ? request.getEndTime().toLocalTime() : null); ts.setHoursWorked(request.getHoursWorked()); ts.setBillableHours(request.getBillableHours() != null ? request.getBillableHours() : 0.0); ts.setWorkSummary(request.getDescription());

        if (request.getTaskId() != null) {
            Task task = taskRepository.findByIdAndCompanyId(request.getTaskId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + request.getTaskId()));
            // ts.setTask(task);
        }

        timesheetRepository.save(ts);
        return TimesheetMapper.toTimesheetResponse(ts);
    }

    @Override
    @Transactional(readOnly = true)
    public TimesheetResponse getById(Long id) {
        return TimesheetMapper.toTimesheetResponse(findInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TimesheetResponse> listMine(Pageable pageable) {
        Long companyId = requireCompanyId();
        Employee emp = employeeRepository.findByUserId(securityUtil.getCurrentUser().getId())
            .orElseThrow(() -> new BadRequestException("Employee profile not found"));
        return timesheetRepository.findByCompanyIdAndEmployeeId(companyId, emp.getId(), pageable)
            .map(TimesheetMapper::toTimesheetResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TimesheetResponse> listForEmployee(Long employeeId, Pageable pageable) {
        return timesheetRepository.findByCompanyIdAndEmployeeId(requireCompanyId(), employeeId, pageable)
            .map(TimesheetMapper::toTimesheetResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimesheetResponse> listByDateRange(Long employeeId, LocalDate from, LocalDate to) {
        return timesheetRepository.findByCompanyIdAndEmployeeIdAndWorkDateBetween(
                requireCompanyId(), employeeId, from, to)
            .stream().map(TimesheetMapper::toTimesheetResponse).toList();
    }

    @Override
    @Transactional
    public TimesheetResponse update(Long id, TimesheetRequest request) {
        Long companyId = requireCompanyId();
        Timesheet ts = findInTenant(id);
        if (ts.isApproved()) {
            throw new BadRequestException("Cannot edit an approved timesheet");
        }
        if (request.getStartTime()    != null) ts.setStartTime(request.getStartTime().toLocalTime());
        if (request.getEndTime()      != null) ts.setEndTime(request.getEndTime().toLocalTime());
        if (request.getHoursWorked()  != null) ts.setHoursWorked(request.getHoursWorked());
        if (request.getBillableHours()!= null) ts.setBillableHours(request.getBillableHours());
        if (request.getDescription()  != null) ts.setWorkSummary(request.getDescription());
        if (request.getTaskId() != null) {
            // Task relation removed from timesheet
        }
        return TimesheetMapper.toTimesheetResponse(ts);
    }

    @Override
    @Transactional
    public TimesheetResponse approve(Long id) {
        Timesheet ts = findInTenant(id);
        Employee approver = employeeRepository.findByUserId(securityUtil.getCurrentUser().getId())
            .orElseThrow(() -> new BadRequestException("Employee profile not found"));
        ts.setApproved(true);
        ts.setApprovedBy(approver.getUser());
        // ts.setApprovedAt(LocalDateTime.now());
        return TimesheetMapper.toTimesheetResponse(ts);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Timesheet ts = findInTenant(id);
        if (ts.isApproved()) throw new BadRequestException("Cannot delete an approved timesheet");
        ts.softDelete();
    }

    private Timesheet findInTenant(Long id) {
        return timesheetRepository.findByIdAndCompanyId(id, requireCompanyId())
            .orElseThrow(() -> new ResourceNotFoundException("Timesheet not found: " + id));
    }

    private Long requireCompanyId() {
        Long id = securityUtil.getCurrentCompanyId();
        if (id == null) throw new BadRequestException("No company context");
        return id;
    }

    private Company companyRef(Long companyId) {
        Company c = new Company(); c.setId(companyId); return c;
    }
}


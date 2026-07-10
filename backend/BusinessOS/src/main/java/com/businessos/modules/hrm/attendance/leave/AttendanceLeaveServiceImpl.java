package com.businessos.modules.hrm.attendance.leave;

import com.businessos.auth.user.User;
import com.businessos.auth.user.UserRepository;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceLeaveServiceImpl implements AttendanceLeaveService {

    private final AttendanceLeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public AttendanceLeaveResponse create(AttendanceLeaveRequest request) {
        Long companyId = securityUtil.getCurrentCompanyId();

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        AttendanceLeave leave = AttendanceLeave.builder()
                .companyId(companyId)
                .employee(employee)
                .leaveDate(request.getLeaveDate())
                .leaveType(request.getLeaveType())
                .leaveReason(request.getLeaveReason())
                .halfDay(request.isHalfDay())
                .approved(false)
                .notes(request.getNotes())
                .build();

        leave = leaveRepository.save(leave);
        return AttendanceLeaveMapper.toResponse(leave);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceLeaveResponse getById(Long id) {
        AttendanceLeave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));
        return AttendanceLeaveMapper.toResponse(leave);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceLeaveResponse> getAll(Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return leaveRepository.findByCompanyId(companyId, pageable)
                .map(AttendanceLeaveMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceLeaveResponse> getByEmployeeId(Long employeeId, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return leaveRepository.findByCompanyIdAndEmployeeId(companyId, employeeId, pageable)
                .map(AttendanceLeaveMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceLeaveResponse> getPendingLeaves() {
        Long companyId = securityUtil.getCurrentCompanyId();
        return leaveRepository.findByCompanyIdAndApprovedFalse(companyId)
                .stream()
                .map(AttendanceLeaveMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceLeaveResponse> getLeavesByDateRange(LocalDate start, LocalDate end, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return leaveRepository.findByCompanyIdAndLeaveDateBetween(companyId, start, end, pageable)
                .map(AttendanceLeaveMapper::toResponse);
    }

    @Override
    @Transactional
    public AttendanceLeaveResponse approveLeave(Long id) {
        AttendanceLeave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));

        User currentUser = securityUtil.getCurrentUser();
        if (currentUser == null) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        Long currentUserId = currentUser.getId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        leave.setApproved(true);
        leave.setApprovedBy(user.getFullName());
        leave.setApprovedDate(LocalDate.now());
        leave = leaveRepository.save(leave);
        return AttendanceLeaveMapper.toResponse(leave);
    }

    @Override
    @Transactional
    public AttendanceLeaveResponse rejectLeave(Long id, String reason) {
        AttendanceLeave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));

        leave.setApproved(false);
        leave.setRejectionReason(reason);
        leave = leaveRepository.save(leave);
        return AttendanceLeaveMapper.toResponse(leave);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AttendanceLeave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));
        leave.setDeleted(true);
        leaveRepository.save(leave);
    }

    @Override
    @Transactional
    public AttendanceLeaveResponse update(Long id, AttendanceLeaveRequest request) {
        AttendanceLeave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));
        
        leave.setLeaveDate(request.getLeaveDate());
        leave.setLeaveType(request.getLeaveType());
        leave.setLeaveReason(request.getLeaveReason());
        leave.setHalfDay(request.isHalfDay());
        leave.setNotes(request.getNotes());
        
        leave = leaveRepository.save(leave);
        return AttendanceLeaveMapper.toResponse(leave);
    }
}


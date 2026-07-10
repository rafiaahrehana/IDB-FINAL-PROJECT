package com.businessos.modules.hrm.attendance.leave;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceLeaveService {
    AttendanceLeaveResponse create(AttendanceLeaveRequest request);
    AttendanceLeaveResponse update(Long id, AttendanceLeaveRequest request);
    void delete(Long id);
    AttendanceLeaveResponse getById(Long id);
    Page<AttendanceLeaveResponse> getAll(Pageable pageable);
    Page<AttendanceLeaveResponse> getByEmployeeId(Long employeeId, Pageable pageable);
    List<AttendanceLeaveResponse> getPendingLeaves();
    Page<AttendanceLeaveResponse> getLeavesByDateRange(LocalDate start, LocalDate end, Pageable pageable);
    AttendanceLeaveResponse approveLeave(Long id);
    AttendanceLeaveResponse rejectLeave(Long id, String reason);
}

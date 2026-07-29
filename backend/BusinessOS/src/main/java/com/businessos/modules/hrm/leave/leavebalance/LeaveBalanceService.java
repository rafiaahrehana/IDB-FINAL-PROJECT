package com.businessos.modules.hrm.leave.leavebalance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeaveBalanceService {

    // ADMIN/OWNER: create a leave balance entry for an employee
    LeaveBalanceResponse create(LeaveBalanceRequest request);

    // ADMIN/OWNER: update an existing leave balance entry
    LeaveBalanceResponse update(Long id, LeaveBalanceRequest request);

    // ADMIN/OWNER: delete a leave balance entry
    void delete(Long id);

    // ADMIN/OWNER: list all leave balances for the company for a given year
    Page<LeaveBalanceResponse> listAll(int year, Pageable pageable);
}

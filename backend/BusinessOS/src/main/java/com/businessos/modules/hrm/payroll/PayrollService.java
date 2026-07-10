package com.businessos.modules.hrm.payroll;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayrollService {
    PayrollResponse create(CreatePayrollRequest request);
    PayrollResponse getById(Long id);
    Page<PayrollResponse> listByPeriod(int month, int year, Pageable pageable);
    Page<PayrollResponse> listForEmployee(Long employeeId, Pageable pageable);
    PayrollResponse approve(Long id);
    PayrollResponse markPaid(Long id, String paymentReference);
    void delete(Long id);
}

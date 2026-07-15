package com.businessos.modules.hrm.payroll;

import com.businessos.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayrollService {
    PayrollResponse create(CreatePayrollRequest request);
    PayrollResponse getById(Long id);
    Page<PayrollResponse> listByPeriod(int month, int year, Pageable pageable);
    Page<PayrollResponse> listForEmployee(Long employeeId, Pageable pageable);
    PayrollResponse approve(Long id);
    PayrollResponse markPaid(Long id, String paymentReference, PaymentMethod paymentMethod);
    void delete(Long id);

    /** Creates a DRAFT payroll for every active employee with a salary structure who doesn't already have one for this period. */
    BulkPayrollResult generateForAllEmployees(int month, int year);
}

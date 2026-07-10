package com.businessos.modules.hrm.payroll;

import com.businessos.auth.user.User;
import com.businessos.modules.hrm.employee.Employee;

public class PayrollMapper {
    public static PayrollResponse toPayrollResponse(Payroll p) {
        Employee emp = p.getEmployee();
        User empUser = emp != null ? emp.getUser() : null;
        Employee approver = p.getApprovedBy();
        User approverUser = approver != null ? approver.getUser() : null;
        PayrollResponse r = new PayrollResponse();
        r.setId(p.getId());
        r.setPayMonth(p.getPayMonth());
        r.setPayYear(p.getPayYear());
        r.setBasicSalary(p.getBasicSalary());
        r.setHouseRent(p.getHouseRent());
        r.setMedicalAllowance(p.getMedicalAllowance());
        r.setTransportAllowance(p.getTransportAllowance());
        r.setBonus(p.getBonus());
        r.setDeductions(p.getDeductions());
        r.setTaxDeduction(p.getTaxDeduction());
        r.setNetSalary(p.getNetSalary());
        r.setStatus(p.getStatus());
        r.setPaymentReference(p.getPaymentReference());
        r.setPaidAt(p.getPaidAt());
        r.setNotes(p.getNotes());
        r.setEmployeeId(emp != null ? emp.getId() : null);
        r.setEmployeeName(empUser != null ? empUser.getFullName() : null);
        r.setApprovedById(approver != null ? approver.getId() : null);
        r.setApprovedByName(approverUser != null ? approverUser.getFullName() : null);
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}

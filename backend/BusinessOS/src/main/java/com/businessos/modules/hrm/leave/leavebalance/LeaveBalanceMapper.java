package com.businessos.modules.hrm.leave.leavebalance;

public class LeaveBalanceMapper {
    public static LeaveBalanceResponse toLeaveBalanceResponse(LeaveBalance lb) {
        LeaveBalanceResponse r = new LeaveBalanceResponse();
        r.setId(lb.getId());
        r.setLeaveType(lb.getLeaveType());
        r.setYear(lb.getYear());
        r.setEntitledDays(lb.getTotalDays());
        r.setUsedDays(lb.getUsedDays());
        r.setPendingDays(lb.getPendingDays());
        r.setRemainingDays(lb.getRemainingDays());
        return r;
    }
}

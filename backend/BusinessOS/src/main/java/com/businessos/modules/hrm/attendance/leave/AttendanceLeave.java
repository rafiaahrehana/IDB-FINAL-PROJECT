package com.businessos.modules.hrm.attendance.leave;

import com.businessos.core.base.BaseEntity;
import com.businessos.modules.hrm.employee.Employee;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import java.time.LocalDate;

@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "companyId", type = Long.class))
@Filter(name = "tenantFilter", condition = "company_id = :companyId")
@Entity
@Table(name = "attendance_leaves")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceLeave extends BaseEntity {

    private Long companyId; // Tenant isolation

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private LocalDate leaveDate;
    private String leaveType; // Sick, Personal, Annual, Maternity, etc.

    private String leaveReason;

    @Builder.Default
    private boolean halfDay = false;

    @Builder.Default
    private boolean approved = false;

    private String approvedBy;
    private LocalDate approvedDate;

    private String rejectionReason;

    private String notes;
}

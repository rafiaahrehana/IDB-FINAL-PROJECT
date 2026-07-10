package com.businessos.modules.hrm.recruitment.jobpost;

import com.businessos.modules.hrm.department.Department;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.auth.user.User;
import org.springframework.stereotype.Component;

@Component
public class JobPostingMapper {
    public static JobPostingResponse toResponse(JobPosting j) {
        Department dept = j.getDepartment();
        Employee creator = j.getCreatedBy();
        User creatorUser = creator != null ? creator.getUser() : null;
        JobPostingResponse r = new JobPostingResponse();
        r.setId(j.getId());
        r.setTitle(j.getTitle());
        r.setJobTitle(j.getJobTitle());
        r.setDescription(j.getDescription());
        r.setRequirements(j.getRequirements());
        r.setEmploymentType(j.getEmploymentType());
        r.setStatus(j.getStatus());
        r.setVacancies(j.getVacancies());
        r.setSalaryMin(j.getSalaryMin());
        r.setSalaryMax(j.getSalaryMax());
        r.setDeadline(j.getDeadline());
        r.setRemote(j.getRemote());
        r.setDepartmentId(dept != null ? dept.getId() : null);
        r.setDepartmentName(dept != null ? dept.getName() : null);
        r.setCreatedById(creator != null ? creator.getId() : null);
        r.setCreatedByName(creatorUser != null ? creatorUser.getFullName() : null);
        r.setCreatedAt(j.getCreatedAt());
        return r;
    }
}

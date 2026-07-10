package com.businessos.modules.hrm.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    /** ADMIN / OWNER: onboard a new employee — creates User + Employee in one transaction */
    EmployeeResponse create(CreateEmployeeRequest request);

    /** ADMIN / OWNER: get full employee details by id */
    EmployeeResponse getById(Long id);

    /** EMPLOYEE: get own profile */
    EmployeeResponse getMyProfile();

    /** ADMIN / OWNER: list all employees, optionally filtered by department */
    Page<EmployeeResponse> listAll(Long departmentId, Pageable pageable);

    /** ADMIN / OWNER: update employee profile fields and relationships */
    EmployeeResponse update(Long id, UpdateEmployeeRequest request);

    /** ADMIN / OWNER: terminate employment — sets requeststatus TERMINATED and soft-deletes platformuser */
    void terminate(Long id);

    /** ADMIN / OWNER: get total number of employees for the current company */
    long getEmployeeCount();

    /** Checks if a platformuser is an employee of the current company */
    boolean isEmployee(Long userId);
}

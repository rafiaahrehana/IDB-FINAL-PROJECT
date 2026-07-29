package com.businessos.modules.hrm.employee;

import com.businessos.enums.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUserId(Long userId);

    Optional<Employee> findByIdAndCompanyId(Long id, Long companyId);

    Optional<Employee> findByCompanyIdAndEmployeeNumber(Long companyId, String employeeNumber);

    boolean existsByUserIdAndCompanyId(Long userId, Long companyId);

    Page<Employee> findByCompanyId(Long companyId, Pageable pageable);

    List<Employee> findByCompanyIdAndActiveTrue(Long companyId);

    Page<Employee> findByCompanyIdAndDepartmentId(Long companyId, Long departmentId, Pageable pageable);

    // excludeOwner support - the company owner gets an auto-created Employee record
    // (so leave/timesheet/expense/payroll "my work" lookups don't 404 for them), but
    // the HRM Employees admin page shouldn't list the owner as a manageable employee.
    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN e.user u " +
           "WHERE e.company.id = :companyId " +
           "AND (:excludedUserId IS NULL OR u.id IS NULL OR u.id != :excludedUserId) " +
           "AND e.deleted = false")
    Page<Employee> findByCompanyIdExcludingOwner(
            @Param("companyId") Long companyId,
            @Param("excludedUserId") Long excludedUserId,
            Pageable pageable);

    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN e.user u " +
           "LEFT JOIN e.department d " +
           "WHERE e.company.id = :companyId " +
           "AND d.id = :departmentId " +
           "AND (:excludedUserId IS NULL OR u.id IS NULL OR u.id != :excludedUserId) " +
           "AND e.deleted = false")
    Page<Employee> findByCompanyIdAndDepartmentIdExcludingOwner(
            @Param("companyId") Long companyId,
            @Param("departmentId") Long departmentId,
            @Param("excludedUserId") Long excludedUserId,
            Pageable pageable);

    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN e.user u " +
           "WHERE e.company.id = :companyId " +
           "AND e.employmentStatus = :status " +
           "AND (:excludedUserId IS NULL OR u.id IS NULL OR u.id != :excludedUserId) " +
           "AND e.deleted = false")
    Page<Employee> findByCompanyIdAndEmploymentStatusExcludingOwner(
            @Param("companyId") Long companyId,
            @Param("status") EmploymentStatus status,
            @Param("excludedUserId") Long excludedUserId,
            Pageable pageable);

    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN e.user u " +
           "LEFT JOIN e.department d " +
           "WHERE e.company.id = :companyId " +
           "AND d.id = :departmentId " +
           "AND e.employmentStatus = :status " +
           "AND (:excludedUserId IS NULL OR u.id IS NULL OR u.id != :excludedUserId) " +
           "AND e.deleted = false")
    Page<Employee> findByCompanyIdAndDepartmentIdAndEmploymentStatusExcludingOwner(
            @Param("companyId") Long companyId,
            @Param("departmentId") Long departmentId,
            @Param("status") EmploymentStatus status,
            @Param("excludedUserId") Long excludedUserId,
            Pageable pageable);

    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN e.user u " +
           "LEFT JOIN e.department d " +
           "LEFT JOIN e.designation des " +
           "WHERE e.company.id = :companyId " +
           "AND (:departmentId IS NULL OR d.id = :departmentId) " +
           "AND e.employmentStatus = :status " +
           "AND (:ownerUserId IS NULL OR u.id != :ownerUserId) " +
           "AND (:search IS NULL OR (" +
           "    LOWER(e.employeeNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    CAST(e.id AS string) LIKE CONCAT('%', :search, '%') OR " +
           "    LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    LOWER(e.officialEmail) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    LOWER(e.jobTitle) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    CAST(d.id AS string) LIKE CONCAT('%', :search, '%') OR " +
           "    LOWER(des.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    LOWER(des.code) LIKE LOWER(CONCAT('%', :search, '%'))" +
           ")) " +
           "AND e.deleted = false")
    Page<Employee> searchEmployeesWithStatus(
            @Param("companyId") Long companyId,
            @Param("departmentId") Long departmentId,
            @Param("status") EmploymentStatus status,
            @Param("ownerUserId") Long ownerUserId,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN e.user u " +
           "LEFT JOIN e.department d " +
           "LEFT JOIN e.designation des " +
           "WHERE e.company.id = :companyId " +
           "AND (:departmentId IS NULL OR d.id = :departmentId) " +
           "AND (:ownerUserId IS NULL OR u.id != :ownerUserId) " +
           "AND (:search IS NULL OR (" +
           "    LOWER(e.employeeNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    CAST(e.id AS string) LIKE CONCAT('%', :search, '%') OR " +
           "    LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    LOWER(e.officialEmail) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    LOWER(e.jobTitle) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    CAST(d.id AS string) LIKE CONCAT('%', :search, '%') OR " +
           "    LOWER(des.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "    LOWER(des.code) LIKE LOWER(CONCAT('%', :search, '%'))" +
           ")) " +
           "AND e.deleted = false")
    Page<Employee> searchEmployeesWithoutStatus(
            @Param("companyId") Long companyId,
            @Param("departmentId") Long departmentId,
            @Param("ownerUserId") Long ownerUserId,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.company.id = :companyId AND e.deleted = false")
    long countByCompanyId(@Param("companyId") Long companyId);

    long countByCompanyIdAndUserIdNot(Long companyId, Long excludedUserId);

    @Query("SELECT e FROM Employee e WHERE e.department.name = :departmentName AND e.active = true")
    List<Employee> findByDepartment(@Param("departmentName") String departmentName);

    /**
     * Resolves the company ID for a platformuser who is an employee.
     * Used by AuthServiceImpl.resolveCompanyId() in Phase 3+.
     */
    @Query("SELECT e.company.id FROM Employee e WHERE e.user.id = :userId AND e.active = true AND e.deleted = false")
    Optional<Long> findCompanyIdByUserId(Long userId);
    boolean existsByCompanyId(Long companyId);

    /**
     * Used by EmployeeNumberGenerator - MAX-based (not COUNT) to be safe against
     * concurrent inserts and deleted records skewing the sequence.
     */
    @Query("SELECT MAX(e.employeeNumber) FROM Employee e WHERE e.company.id = :companyId AND e.employeeNumber LIKE CONCAT(:prefix, '%')")
    Optional<String> findMaxEmployeeNumberByCompanyAndPrefix(@Param("companyId") Long companyId, @Param("prefix") String prefix);

    /**
     * Used by EmployeeNumberBackfillInitializer to find pre-existing employees
     * that predate auto-generation and never had a number set.
     */
    @Query("SELECT e FROM Employee e WHERE e.company.id = :companyId AND (e.employeeNumber IS NULL OR e.employeeNumber = '') ORDER BY e.id ASC")
    List<Employee> findByCompanyIdWithBlankEmployeeNumber(@Param("companyId") Long companyId);
}

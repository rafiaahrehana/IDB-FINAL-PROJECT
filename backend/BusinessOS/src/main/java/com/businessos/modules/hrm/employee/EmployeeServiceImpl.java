package com.businessos.modules.hrm.employee;

import com.businessos.modules.company.Company;
import com.businessos.modules.hrm.department.Department;
import com.businessos.modules.hrm.designation.Designation;
import com.businessos.modules.hrm.attendance.shift.Shift;
import com.businessos.auth.user.User;
import com.businessos.enums.EmploymentStatus;
import com.businessos.auth.role.enums.Role;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.modules.hrm.department.DepartmentRepository;
import com.businessos.modules.hrm.designation.DesignationRepository;
import com.businessos.modules.hrm.attendance.shift.ShiftRepository;
import com.businessos.auth.user.UserRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.email.EmailService;
import com.businessos.shared.email.EmailBranding;
import com.businessos.shared.notification.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final ShiftRepository shiftRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EmailBranding emailBranding;
    private final NotificationPreferenceService notificationPreferenceService;
    private final EmployeeMapper employeeMapper;
    private final com.businessos.shared.address.LocationMapper locationMapper;
    private final SecurityUtil securityUtil;

    private Long requireCompanyId() {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null)
            throw new BadRequestException("No company context found in security context.");
        return companyId;
    }

    private Company findCompanyById(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
    }

    private Employee findEmployeeById(Long id) {
        Long companyId = requireCompanyId();
        return employeeRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    private Employee findCurrentEmployee() {
        User user = securityUtil.getCurrentUser();
        return employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found."));
    }

    private void validateEmployeeCreation(CreateEmployeeRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(normalizedEmail))
            throw new BadRequestException("An account with this email already exists.");
    }

    private void validateNotSelfManager(Long employeeId, Long managerId) {
        if (managerId.equals(employeeId))
            throw new BadRequestException("An employee cannot be their own reporting manager.");
    }

    private User createPortalUser(CreateEmployeeRequest request) {
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.EMPLOYEE)
                .active(true)
                .emailVerified(true)
                .build();
        userRepository.save(user);

        return user;
    }

    private Employee buildEmployee(CreateEmployeeRequest request, User user, Company company) {
        return Employee.builder()
                .user(user)
                .company(company)
                .employeeNumber(request.getEmployeeNumber())
                .officialEmail(request.getOfficialEmail())
                .workPhone(request.getWorkPhone())
                .profileImageUrl(request.getProfileImageUrl())
                .nationalId(request.getNationalId())
                .taxId(request.getTaxId())
                .costCenter(request.getCostCenter())
                .officeLocation(request.getOfficeLocation())
                .jobTitle(request.getJobTitle())
                .employmentType(request.getEmploymentType())
                .employmentStatus(request.getEmploymentStatus() != null
                        ? request.getEmploymentStatus()
                        : EmploymentStatus.PROBATION)
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .fatherName(request.getFatherName())
                .motherName(request.getMotherName())
                .location(locationMapper.toEntity(request.getLocation()))
                .hireDate(request.getHireDate())
                .confirmationDate(request.getConfirmationDate())
                .probationEndDate(request.getProbationEndDate())
                .contractEndDate(request.getContractEndDate())
                .basicSalary(request.getBasicSalary())
                .houseRent(request.getHouseRent())
                .medicalAllowance(request.getMedicalAllowance())
                .transportAllowance(request.getTransportAllowance())
                .bankName(request.getBankName())
                .bankAccountNumber(request.getBankAccountNumber())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .emergencyContactRelation(request.getEmergencyContactRelation())
                .build();
    }

    private void assignEmployeeRelationships(Employee employee, CreateEmployeeRequest request, Long companyId) {
        if (request.getDepartmentId() != null)
            employee.setDepartment(findDepartmentById(request.getDepartmentId(), companyId));
        if (request.getDesignationId() != null)
            employee.setDesignation(findDesignationById(request.getDesignationId(), companyId));
        if (request.getReportingManagerId() != null)
            employee.setReportingManager(findReportingManagerById(request.getReportingManagerId(), companyId));
        if (request.getShiftId() != null)
            employee.setShift(findShiftById(request.getShiftId(), companyId));
    }

    private void sendWelcomeEmail(User user, Company company) {
        try {
            // company is already loaded — no redundant DB lookup needed
            EmailBranding.Data branding = emailBranding.from(company);
            emailService.sendEmployeeWelcomeEmail(user.getEmail(), user.getFirstName(), branding);
        } catch (Exception ex) {
            // Email failure must not fail employee creation — log and continue
            log.error("Welcome email failed for platformuser {}: {}", user.getEmail(), ex.getMessage());
        }
    }

    private void updateEmployeeDetails(Employee emp, UpdateEmployeeRequest request) {
        if (request.getJobTitle() != null)
            emp.setJobTitle(request.getJobTitle());
        if (request.getEmploymentType() != null)
            emp.setEmploymentType(request.getEmploymentType());
        if (request.getEmploymentStatus() != null)
            emp.setEmploymentStatus(request.getEmploymentStatus());
        if (request.getGender() != null)
            emp.setGender(request.getGender());
        if (request.getDateOfBirth() != null)
            emp.setDateOfBirth(request.getDateOfBirth());
        if (request.getFatherName() != null)
            emp.setFatherName(request.getFatherName());
        if (request.getMotherName() != null)
            emp.setMotherName(request.getMotherName());
        if (request.getLocation() != null) {
            if (emp.getLocation() == null) {
                emp.setLocation(locationMapper.toEntity(request.getLocation()));
            } else {
                locationMapper.updateEntityFromRequest(emp.getLocation(), request.getLocation());
            }
        }
        if (request.getHireDate() != null)
            emp.setHireDate(request.getHireDate());
        if (request.getConfirmationDate() != null)
            emp.setConfirmationDate(request.getConfirmationDate());
        if (request.getProbationEndDate() != null)
            emp.setProbationEndDate(request.getProbationEndDate());
        if (request.getContractEndDate() != null)
            emp.setContractEndDate(request.getContractEndDate());
        if (request.getBasicSalary() != null)
            emp.setBasicSalary(request.getBasicSalary());
        if (request.getHouseRent() != null)
            emp.setHouseRent(request.getHouseRent());
        if (request.getMedicalAllowance() != null)
            emp.setMedicalAllowance(request.getMedicalAllowance());
        if (request.getTransportAllowance() != null)
            emp.setTransportAllowance(request.getTransportAllowance());
        if (request.getBankName() != null)
            emp.setBankName(request.getBankName());
        if (request.getBankAccountNumber() != null)
            emp.setBankAccountNumber(request.getBankAccountNumber());
        if (request.getEmergencyContactName() != null)
            emp.setEmergencyContactName(request.getEmergencyContactName());
        if (request.getEmergencyContactPhone() != null)
            emp.setEmergencyContactPhone(request.getEmergencyContactPhone());
        if (request.getEmergencyContactRelation() != null)
            emp.setEmergencyContactRelation(request.getEmergencyContactRelation());
        if (request.getNationalId() != null)
            emp.setNationalId(request.getNationalId());
        if (request.getTaxId() != null)
            emp.setTaxId(request.getTaxId());
        if (request.getCostCenter() != null)
            emp.setCostCenter(request.getCostCenter());
        if (request.getOfficeLocation() != null)
            emp.setOfficeLocation(request.getOfficeLocation());
        if (request.getWorkPhone() != null)
            emp.setWorkPhone(request.getWorkPhone());
        if (request.getOfficialEmail() != null)
            emp.setOfficialEmail(request.getOfficialEmail());
        if (request.getProfileImageUrl() != null)
            emp.setProfileImageUrl(request.getProfileImageUrl());
    }

    private void updateEmployeeRelationships(Employee emp, UpdateEmployeeRequest request, Long companyId) {
        if (request.getDepartmentId() != null)
            emp.setDepartment(findDepartmentById(request.getDepartmentId(), companyId));
        if (request.getDesignationId() != null)
            emp.setDesignation(findDesignationById(request.getDesignationId(), companyId));
        if (request.getReportingManagerId() != null) {
            validateNotSelfManager(emp.getId(), request.getReportingManagerId());
            emp.setReportingManager(findReportingManagerById(request.getReportingManagerId(), companyId));
        }
        if (request.getShiftId() != null)
            emp.setShift(findShiftById(request.getShiftId(), companyId));
    }

    private void deactivatePortalUser(Employee emp) {
        User user = emp.getUser();
        if (user == null)
            return;
        user.setActive(false);
        user.softDelete();
        userRepository.save(user);
    }

    private Department findDepartmentById(Long departmentId, Long companyId) {
        return departmentRepository.findByIdAndCompanyId(departmentId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
    }

    private Designation findDesignationById(Long designationId, Long companyId) {
        return designationRepository.findByIdAndCompanyId(designationId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found with id: " + designationId));
    }

    private Shift findShiftById(Long shiftId, Long companyId) {
        return shiftRepository.findByIdAndCompanyId(shiftId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + shiftId));
    }

    private Employee findReportingManagerById(Long managerId, Long companyId) {
        return employeeRepository.findByIdAndCompanyId(managerId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporting manager not found with id: " + managerId));
    }

    @Override
    @Transactional
    public EmployeeResponse create(CreateEmployeeRequest request) {

        Long companyId = requireCompanyId();
        validateEmployeeCreation(request);
        Company company = findCompanyById(companyId);
        User user = createPortalUser(request);
        Employee employee = buildEmployee(request, user, company);
        assignEmployeeRelationships(employee, request, companyId);
        employeeRepository.save(employee);
        notificationPreferenceService.createDefaultsForUser(user.getId());
        sendWelcomeEmail(user, company);

        return employeeMapper.toDTO(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {

        return employeeMapper.toDTO(findEmployeeById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getMyProfile() {

        return employeeMapper.toDTO(findCurrentEmployee());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> listAll(Long departmentId, Pageable pageable) {
        Long companyId = requireCompanyId();

        if (departmentId != null)
            return employeeRepository.findByCompanyIdAndDepartmentId(companyId, departmentId, pageable)
                    .map(employeeMapper::toDTO);
        return employeeRepository.findByCompanyId(companyId, pageable)
                .map(employeeMapper::toDTO);
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, UpdateEmployeeRequest request) {

        Long companyId = requireCompanyId();
        Employee emp = findEmployeeById(id);
        updateEmployeeDetails(emp, request);
        updateEmployeeRelationships(emp, request, companyId);
        employeeRepository.save(emp);

        return employeeMapper.toDTO(emp);
    }

    @Override
    @Transactional
    public void terminate(Long id) {

        Employee emp = findEmployeeById(id);
        emp.setActive(false);
        emp.setEmploymentStatus(EmploymentStatus.TERMINATED);
        emp.softDelete();
        deactivatePortalUser(emp);

        if (emp.getUser() != null) {
            try {
                EmailBranding.Data branding = emailBranding.from(emp.getCompany());
                emailService.sendTerminationEmail(
                        emp.getUser().getEmail(), emp.getUser().getFirstName(), branding);
            } catch (Exception ex) {
                throw new com.businessos.shared.exception.BadRequestException(
                        "Internal error during operation: " + ex.getMessage());
            }
        }

    }

    @Override
    @Transactional(readOnly = true)
    public long getEmployeeCount() {
        return employeeRepository.countByCompanyId(requireCompanyId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmployee(Long userId) {
        return employeeRepository.existsByUserIdAndCompanyId(userId, requireCompanyId());
    }
}

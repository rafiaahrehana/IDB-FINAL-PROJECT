package com.businessos.modules.hrm.employee;

import com.businessos.enums.EmploymentStatus;
import com.businessos.enums.EmploymentType;
import com.businessos.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateEmployeeRequest {
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50)
    private String firstName;
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50)
    private String lastName;
    @NotBlank(message = "Email is required")
    @Email
    private String email;
    @NotBlank(message = "Password is required")
    @Size(min = 8)
    private String password;
    @Size(max = 30)
    private String employeeNumber;
    @Email
    @Size(max = 255)
    private String officialEmail;
    @Size(max = 30)
    private String workPhone;
    @Size(max = 500)
    private String profileImageUrl;
    @Size(max = 50)
    private String nationalId;
    @Size(max = 50)
    private String taxId;
    @Size(max = 100)
    private String costCenter;
    @Size(max = 150)
    private String officeLocation;
    @Size(max = 100)
    private String jobTitle;
    private Long designationId;
    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;
    private EmploymentStatus employmentStatus;
    private Long departmentId;
    private Long reportingManagerId;
    private Long shiftId;
    private Gender gender;
    private LocalDate dateOfBirth;
    @Size(max = 100)
    private String fatherName;
    @Size(max = 100)
    private String motherName;
    private com.businessos.shared.address.LocationRequest location;
    private LocalDate hireDate;
    private LocalDate confirmationDate;
    private LocalDate probationEndDate;
    private LocalDate contractEndDate;
    private BigDecimal basicSalary;
    private BigDecimal houseRent;
    private BigDecimal medicalAllowance;
    private BigDecimal transportAllowance;
    @Size(max = 100)
    private String bankName;
    @Size(max = 100)
    private String bankAccountNumber;
    @Size(max = 100)
    private String emergencyContactName;
    @Size(max = 30)
    private String emergencyContactPhone;
    @Size(max = 50)
    private String emergencyContactRelation;
}

package com.businessos.modules.ai.prompt;

import com.businessos.modules.ai.exception.AiPromptException;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;


@Setter
@Accessors(chain = true)
public class EmploymentLetterPromptBuilder {

    private String companyName;
    private String employeeName;
    private String designation;
    private String department;
    private LocalDate joiningDate;
    private String letterType;

    public static EmploymentLetterPromptBuilder builder() {
        return new EmploymentLetterPromptBuilder();
    }

    public String build() {
        validateFields();
        return """
            Generate a professional, legally compliant employment %s letter.

            Company Name     : %s
            Employee Name    : %s
            Designation      : %s
            Department       : %s
            Date of Joining  : %s

            Output instructions:
            - Use formal language throughout.
            - Include standard HR clauses for this letter type.
            - Leave a signature block at the bottom for the authorised signatory.
            - Return only the letter body — no preamble, no explanation.
            """.formatted(
                letterType,
                companyName,
                employeeName,
                designation,
                department,
                joiningDate
            );
    }

    private void validateFields() {
        if (companyName  == null || companyName.isBlank())
            throw new AiPromptException("companyName is required for employment letter prompt");
        if (employeeName == null || employeeName.isBlank())
            throw new AiPromptException("employeeName is required for employment letter prompt");
        if (designation  == null || designation.isBlank())
            throw new AiPromptException("designation is required for employment letter prompt");
        if (joiningDate  == null)
            throw new AiPromptException("joiningDate is required for employment letter prompt");
        if (letterType   == null || letterType.isBlank())
            throw new AiPromptException("letterType is required for employment letter prompt");
    }
}

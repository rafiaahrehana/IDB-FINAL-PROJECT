package com.businessos.modules.ai.prompt;

import com.businessos.modules.ai.exception.AiPromptException;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * FIX: Added missing import for AiPromptException.
 * validateFields() throws AiPromptException but had no import for it,
 * causing "cannot find symbol class AiPromptException".
 */
@Setter
@Accessors(chain = true)
public class CrmSummaryPromptBuilder {

    private String contactName;
    private String companyName;
    private String currentStatus;
    private String activityHistory;
    private String interestedService;

    public static CrmSummaryPromptBuilder builder() {
        return new CrmSummaryPromptBuilder();
    }

    public String build() {
        validateFields();
        return """
            Summarise the following CRM lead and suggest the next best action.

            Contact Name       : %s
            Company            : %s
            Current Status     : %s
            Interested Service : %s
            Activity History   :
            %s

            Output instructions:
            - Provide a 2–3 sentence summary of the lead's engagement so far.
            - Recommend one specific next action for the sales team.
            - Return only the summary and recommendation — no preamble.
            """.formatted(
                contactName,
                companyName       != null ? companyName       : "Unknown",
                currentStatus,
                interestedService != null ? interestedService : "Not specified",
                activityHistory   != null ? activityHistory   : "No activity recorded yet"
            );
    }

    private void validateFields() {
        if (contactName   == null || contactName.isBlank())
            throw new AiPromptException("contactName is required for CRM summary prompt");
        if (currentStatus == null || currentStatus.isBlank())
            throw new AiPromptException("currentStatus is required for CRM summary prompt");
    }
}

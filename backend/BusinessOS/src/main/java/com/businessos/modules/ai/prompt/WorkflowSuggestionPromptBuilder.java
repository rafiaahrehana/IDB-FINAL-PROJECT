package com.businessos.modules.ai.prompt;

import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(chain = true)
public class WorkflowSuggestionPromptBuilder {

    private String goal;
    private String existingTemplatesSummary;

    public static WorkflowSuggestionPromptBuilder builder() {
        return new WorkflowSuggestionPromptBuilder();
    }

    public String build() {
        validateFields();
        return """
            Suggest a service workflow (an ordered list of stages) for this company.

            What the company wants the workflow for:
            %s

            The company's existing workflow templates (for consistency - reuse similar
            stage naming and structure where it makes sense, avoid duplicating an
            already-covered process):
            %s

            Output instructions:
            - Suggest a short workflow name, then a numbered list of stages in order.
            - For each stage give: name, a one-line purpose, and whether it typically needs approval.
            - Keep it to 3-7 stages.
            - Return only the workflow name and stage list - no preamble.
            """.formatted(
                goal,
                PromptSupport.orDefault(existingTemplatesSummary, "No workflow templates configured yet")
            );
    }

    private void validateFields() {
        PromptSupport.requireNonBlank(goal, "goal", "workflow suggestion");
    }
}

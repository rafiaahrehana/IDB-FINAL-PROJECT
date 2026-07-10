package com.businessos.modules.crm.opportunity;

public enum OpportunityStage {
    PROSPECTING(10),
    QUALIFICATION(25),
    NEEDS_ANALYSIS(40),
    PROPOSAL(60),
    NEGOTIATION(80),
    CLOSED_WON(100),
    CLOSED_LOST(0);

    private final int defaultProbability;

    OpportunityStage(int defaultProbability) {
        this.defaultProbability = defaultProbability;
    }

    public int getDefaultProbability() {
        return defaultProbability;
    }

    public boolean isClosed() {
        return this == CLOSED_WON || this == CLOSED_LOST;
    }
}

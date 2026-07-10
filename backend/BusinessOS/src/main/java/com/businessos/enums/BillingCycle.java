package com.businessos.enums;

public enum BillingCycle {

    /** Charged every month. endDate = startDate + 1 month. */
    MONTHLY,

    /** Charged every 3 months. endDate = startDate + 3 months. */
    QUARTERLY,

    /** Charged annually. endDate = startDate + 1 year. */
    YEARLY,

    /** Single charge, no renewal. endDate = startDate + estimatedDays or explicit. */
    ONE_TIME
}

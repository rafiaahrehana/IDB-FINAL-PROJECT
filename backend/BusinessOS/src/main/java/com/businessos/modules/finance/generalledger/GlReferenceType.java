package com.businessos.modules.finance.generalledger;

/**
 * What kind of source document a GeneralLedger entry was posted from. Previously each
 * poster (invoice, payment, expense, payroll, journal entry) passed its own raw String
 * literal to GeneralLedgerService.recordTransaction() - a typo in any one of them would
 * silently create an untracked reference type with no compile-time signal. The enum
 * name() is stored in the existing GeneralLedger.referenceType String column, so no
 * schema change is needed.
 */
public enum GlReferenceType {
    INVOICE,
    INVOICE_CANCEL,
    INVOICE_REFUND,
    PAYMENT_RECEIPT,
    EXPENSE,
    PAYROLL,
    JOURNAL_ENTRY
}

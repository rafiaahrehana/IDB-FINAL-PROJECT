package com.businessos.modules.finance.reconciliation;

public class BankReconciliationMapper {

    public static BankReconciliationResponse toResponse(BankReconciliation entity) {
        if (entity == null) {
            return null;
        }

        return BankReconciliationResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .bankAccountId(entity.getBankAccount() != null ? entity.getBankAccount().getId() : null)
                .bankAccountName(entity.getBankAccount() != null ? entity.getBankAccount().getAccountName() : null)
                .reconciliationDate(entity.getReconciliationDate())
                .glBalance(entity.getGlBalance())
                .bankStatementBalance(entity.getBankStatementBalance())
                .difference(entity.getDifference())
                .outstandingDeposits(entity.getOutstandingDeposits())
                .outstandingChecks(entity.getOutstandingChecks())
                .reconciled(entity.isReconciled())
                .reconciledDate(entity.getReconciledDate())
                .reconciledBy(entity.getReconciledBy())
                .discrepancyNotes(entity.getDiscrepancyNotes())
                .build();
    }
}

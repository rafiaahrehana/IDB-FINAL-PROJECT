package com.businessos.modules.finance.journalentry;

public class JournalEntryMapper {

    public static JournalEntryResponse toResponse(JournalEntry entity) {
        if (entity == null) {
            return null;
        }

        return JournalEntryResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .journalEntryNumber(entity.getJournalEntryNumber())
                .entryDate(entity.getEntryDate())
                .debitAccountId(entity.getDebitAccount() != null ? entity.getDebitAccount().getId() : null)
                .debitAccountName(entity.getDebitAccount() != null ? entity.getDebitAccount().getAccountName() : null)
                .creditAccountId(entity.getCreditAccount() != null ? entity.getCreditAccount().getId() : null)
                .creditAccountName(entity.getCreditAccount() != null ? entity.getCreditAccount().getAccountName() : null)
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .notes(entity.getNotes())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .approvedBy(entity.getApprovedBy())
                .approvedDate(entity.getApprovedDate())
                .approved(entity.isApproved())
                .posted(entity.isPosted())
                .postedDate(entity.getPostedDate())
                .build();
    }
}

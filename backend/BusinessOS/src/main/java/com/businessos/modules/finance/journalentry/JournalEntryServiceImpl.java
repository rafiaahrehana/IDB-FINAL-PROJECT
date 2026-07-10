package com.businessos.modules.finance.journalentry;

import com.businessos.modules.finance.chartofaccounts.ChartOfAccount;
import com.businessos.modules.finance.chartofaccounts.ChartOfAccountRepository;
import com.businessos.modules.finance.generalledger.GeneralLedgerService;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class JournalEntryServiceImpl implements JournalEntryService {

    private final JournalEntryRepository jeRepository;
    private final ChartOfAccountRepository coaRepository;
    private final GeneralLedgerService glService;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public JournalEntryResponse create(JournalEntryRequest request) {
        Long companyId = securityUtil.getCurrentCompanyId();

        // Validate accounts exist
        ChartOfAccount debitAccount = coaRepository.findById(request.getDebitAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Debit account not found"));

        ChartOfAccount creditAccount = coaRepository.findById(request.getCreditAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Credit account not found"));

        String jeNumber = generateJENumber(companyId);

        JournalEntry je = JournalEntry.builder()
                .companyId(companyId)
                .journalEntryNumber(jeNumber)
                .entryDate(request.getEntryDate() != null ? request.getEntryDate() : LocalDate.now())
                .debitAccount(debitAccount)
                .creditAccount(creditAccount)
                .amount(request.getAmount())
                .description(request.getDescription())
                .notes(request.getNotes())
                .createdBy(securityUtil.getCurrentUser().getUsername())
                .createdDate(LocalDate.now())
                .approved(false)
                .posted(false)
                .build();

        je = jeRepository.save(je);
        return JournalEntryMapper.toResponse(je);
    }

    @Override
    @Transactional(readOnly = true)
    public JournalEntryResponse getById(Long id) {
        JournalEntry je = jeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Journal entry not found"));
        return JournalEntryMapper.toResponse(je);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JournalEntryResponse> getAll(Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return jeRepository.findByCompanyId(companyId, pageable)
                .map(JournalEntryMapper::toResponse);
    }

    @Override
    @Transactional
    public void approve(Long id) {
        JournalEntry je = jeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Journal entry not found"));

        if (je.isApproved()) {
            throw new BadRequestException("Journal entry is already approved");
        }

        je.approve(securityUtil.getCurrentUser().getUsername());
        jeRepository.save(je);
    }

    @Override
    @Transactional
    public void post(Long id) {
        JournalEntry je = jeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Journal entry not found"));

        if (!je.isApproved()) {
            throw new BadRequestException("Journal entry must be approved before posting");
        }

        if (je.isPosted()) {
            throw new BadRequestException("Journal entry is already posted");
        }

        // Post to GL
        glService.recordTransaction(
                je.getDebitAccount().getId(),
                je.getAmount(),
                BigDecimal.ZERO,
                je.getDescription(),
                "JOURNAL_ENTRY",
                je.getId(),
                je.getJournalEntryNumber()
        );

        glService.recordTransaction(
                je.getCreditAccount().getId(),
                BigDecimal.ZERO,
                je.getAmount(),
                je.getDescription(),
                "JOURNAL_ENTRY",
                je.getId(),
                je.getJournalEntryNumber()
        );

        je.post();
        jeRepository.save(je);
    }

    @Override
    @Transactional
    public JournalEntryResponse delete(Long id) {
        JournalEntry je = jeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Journal entry not found"));

        if (je.isPosted()) {
            throw new BadRequestException("Cannot delete posted journal entries");
        }

        je.softDelete();
        jeRepository.save(je);
        return JournalEntryMapper.toResponse(je);
    }

    private String generateJENumber(Long companyId) {
        long count = jeRepository.count() + 1;
        return String.format("JE-%04d-%06d", LocalDate.now().getYear(), count);
    }
}


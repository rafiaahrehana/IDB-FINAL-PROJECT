package com.businessos.shared.payment.wallet;

import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import com.businessos.enums.WalletTransactionType;
import com.businessos.shared.exception.BadRequestException;

import com.businessos.security.SecurityUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor

public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository txRepository;
    private final SecurityUtil securityUtil;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional
    public WalletResponse getOrCreateWallet() {
        authorizationService.checkPermission(PermissionCode.WALLET_VIEW);
        Long companyId = requireCompanyId();
        Wallet wallet = walletRepository.findByContextTypeAndContextId("COMPANY", companyId)
            .orElseGet(() -> createWallet("COMPANY", companyId));
        return WalletMapper.toResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WalletTransactionResponse> getTransactions(WalletTransactionType type, Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.WALLET_VIEW);
        Long companyId = requireCompanyId();
        Page<WalletTransaction> page = type != null
            ? txRepository.findByWalletContextTypeAndWalletContextIdAndTypeOrderByTransactedAtDesc("COMPANY", companyId, type, pageable)
            : txRepository.findByWalletContextTypeAndWalletContextIdOrderByTransactedAtDesc("COMPANY", companyId, pageable);
        return page.map(WalletMapper::toTransactionResponse);
    }

    @Override
    @Transactional
    public Wallet debit(String contextType, Long contextId, BigDecimal amount, String reference, String notes) {
        Wallet wallet = walletRepository.findByContextTypeAndContextIdForUpdate(contextType, contextId)
            .orElseThrow(() -> new BadRequestException("Wallet not found for " + contextType + ": " + contextId));

        if (wallet.getTotalAvailable().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient wallet balance");
        }

        // Deduct from credit balance first, then cash balance
        BigDecimal creditUsed = wallet.getCreditBalance().min(amount);
        BigDecimal cashUsed   = amount.subtract(creditUsed);

        wallet.setCreditBalance(wallet.getCreditBalance().subtract(creditUsed));
        wallet.setBalance(wallet.getBalance().subtract(cashUsed));
        walletRepository.save(wallet);

        recordTransaction(wallet, WalletTransactionType.DEBIT, amount,
            wallet.getTotalAvailable(), reference, notes);

        return wallet;
    }

    @Override
    @Transactional
    public Wallet credit(String contextType, Long contextId, BigDecimal amount, WalletTransactionType type,
                          String reference, String notes) {
        Wallet wallet = walletRepository.findByContextTypeAndContextIdForUpdate(contextType, contextId)
            .orElseGet(() -> createWallet(contextType, contextId));

        if (type == WalletTransactionType.CREDIT_APPLIED
                || type == WalletTransactionType.REFUND_CREDIT
                || type == WalletTransactionType.REFERRAL_REWARD) {
            wallet.setCreditBalance(wallet.getCreditBalance().add(amount));
        } else {
            wallet.setBalance(wallet.getBalance().add(amount));
        }
        walletRepository.save(wallet);

        recordTransaction(wallet, type, amount, wallet.getTotalAvailable(), reference, notes);
        return wallet;
    }

    // ── Private helpers ───────────────────────────────────────────

    private Wallet createWallet(String contextType, Long contextId) {
        Wallet w = Wallet.builder().contextType(contextType).contextId(contextId).build();
        return walletRepository.save(w);
    }

    private void recordTransaction(Wallet wallet, WalletTransactionType type,
                                    BigDecimal amount, BigDecimal balanceAfter,
                                    String reference, String notes) {
        WalletTransaction tx = WalletTransaction.builder()
            .wallet(wallet)
            .type(type)
            .amount(amount)
            .balanceAfter(balanceAfter)
            .reference(reference)
            .notes(notes)
            .transactedAt(LocalDateTime.now())
            .build();
        txRepository.save(tx);
    }

    private Long requireCompanyId() {
        Long id = securityUtil.getCurrentCompanyId();
        if (id == null) throw new BadRequestException("No company context");
        return id;
    }
}

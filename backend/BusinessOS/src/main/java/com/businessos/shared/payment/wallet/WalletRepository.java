package com.businessos.shared.payment.wallet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByContextTypeAndContextId(String contextType, Long contextId);

    boolean existsByContextTypeAndContextId(String contextType, Long contextId);
}

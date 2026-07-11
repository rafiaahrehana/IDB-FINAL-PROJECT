package com.businessos.auth.impersonation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImpersonationAuditLogRepository extends JpaRepository<ImpersonationAuditLog, Long> {

    Optional<ImpersonationAuditLog> findByImpersonationSessionId(String impersonationSessionId);
}

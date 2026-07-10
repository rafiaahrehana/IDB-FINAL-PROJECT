package com.businessos.modules.crm.contact;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientContactRepository extends JpaRepository<ClientContact, Long> {

    Optional<ClientContact> findByIdAndCompanyId(Long id, Long companyId);

    List<ClientContact> findByClientIdAndCompanyIdOrderByPrimaryContactDescCreatedAtDesc(Long clientId, Long companyId);

    boolean existsByEmailAndClientIdAndCompanyIdAndDeletedFalse(String email, Long clientId, Long companyId);

    @Modifying
    @Query("UPDATE ClientContact c SET c.primaryContact = false WHERE c.client.id = :clientId AND c.company.id = :companyId")
    void clearPrimaryContact(@Param("clientId") Long clientId, @Param("companyId") Long companyId);
}

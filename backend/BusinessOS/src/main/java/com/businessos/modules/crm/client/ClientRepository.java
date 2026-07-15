package com.businessos.modules.crm.client;

import com.businessos.enums.ClientStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByUserId(Long userId);

    Optional<Client> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByUserIdAndCompanyId(Long userId, Long companyId);

    Page<Client> findByCompanyId(Long companyId, Pageable pageable);

    Page<Client> findByCompanyIdAndStatus(Long companyId, ClientStatus status, Pageable pageable);

    long countByCompanyId(Long companyId);

    @Query("SELECT c FROM Client c WHERE c.company.id = :companyId AND " +
           "(LOWER(c.clientCompanyName) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\' OR " +
           "LOWER(c.user.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\' OR " +
           "LOWER(c.user.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\' OR " +
           "LOWER(c.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\') AND " +
           "c.deleted = false")
    Page<Client> searchClients(@Param("companyId") Long companyId, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c.company.id FROM Client c WHERE c.user.id = :userId AND c.deleted = false")
    Optional<Long> findCompanyIdByUserId(Long userId);
    boolean existsByCompanyId(Long companyId);
}

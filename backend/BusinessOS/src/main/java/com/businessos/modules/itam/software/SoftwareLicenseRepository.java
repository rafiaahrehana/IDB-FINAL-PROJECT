package com.businessos.modules.itam.software;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SoftwareLicenseRepository extends JpaRepository<SoftwareLicense, Long> {

    Optional<SoftwareLicense> findByLicenseKey(String licenseKey);
    Optional<SoftwareLicense> findByCompanyIdAndLicenseKey(Long companyId, String licenseKey);

    Page<SoftwareLicense> findByCompanyIdAndLicenseStatus(Long companyId, LicenseStatus status, Pageable pageable);
    Page<SoftwareLicense> findByCompanyId(Long companyId, Pageable pageable);

    @Query("SELECT s FROM SoftwareLicense s WHERE s.companyId = :companyId AND s.licenseExpiryDate BETWEEN :start AND :end")
    List<SoftwareLicense> findExpiringBetweenDates(@Param("companyId") Long companyId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT s FROM SoftwareLicense s WHERE s.companyId = :companyId AND s.licenseExpiryDate < :date AND s.licenseStatus != 'EXPIRED'")
    List<SoftwareLicense> findExpiredLicenses(@Param("companyId") Long companyId, @Param("date") LocalDate date);

    long countByCompanyIdAndLicenseStatus(Long companyId, LicenseStatus status);
}
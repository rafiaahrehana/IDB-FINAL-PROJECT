package com.businessos.modules.hrm.asset;

import com.businessos.enums.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    Optional<Asset> findByIdAndCompanyId(Long id, Long companyId);

    Page<Asset> findByCompanyId(Long companyId, Pageable pageable);

    Page<Asset> findByCompanyIdAndStatus(Long companyId, AssetStatus status, Pageable pageable);

    List<Asset> findByCompanyIdAndAssignedToId(Long companyId, Long employeeId);

    boolean existsByCompanyIdAndSerialNumber(Long companyId, String serialNumber);

    boolean existsByCompanyIdAndAssetTag(Long companyId, String assetTag);
}

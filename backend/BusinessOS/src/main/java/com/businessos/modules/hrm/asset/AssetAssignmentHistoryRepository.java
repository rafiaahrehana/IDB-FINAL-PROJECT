package com.businessos.modules.hrm.asset;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import com.businessos.modules.itam.shared.AssetHistory;

public interface AssetAssignmentHistoryRepository extends JpaRepository<AssetHistory, Long> {
    Page<AssetHistory> findByAssetIdOrderByAssignedAtDesc(Long assetId, Pageable pageable);
    Page<AssetHistory> findByCompanyIdAndEmployeeIdOrderByAssignedAtDesc(
            Long companyId, Long employeeId, Pageable pageable);
    Optional<AssetHistory> findTopByAssetIdAndReturnedAtIsNullOrderByAssignedAtDesc(Long assetId);
}

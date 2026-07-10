package com.businessos.modules.hrm.asset;

import org.springframework.data.domain.Page;

import java.awt.print.Pageable;

public interface AssetAssignmentHistoryService {

    Page<AssetAssignmentHistoryResponse> historyForAsset(Long assetId, org.springframework.data.domain.Pageable pageable);

    Page<AssetAssignmentHistoryResponse> historyForEmployee(Long employeeId, org.springframework.data.domain.Pageable pageable);

    Page<AssetAssignmentHistoryResponse> historyForAsset(Long assetId, Pageable pageable);

    Page<AssetAssignmentHistoryResponse> historyForEmployee(Long employeeId, Pageable pageable);
}
package com.businessos.modules.hrm.asset;


import com.businessos.shared.exception.BadRequestException;
import com.businessos.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssetAssignmentHistoryServiceImpl implements AssetAssignmentHistoryService {

    private final AssetAssignmentHistoryRepository historyRepository;
    private final SecurityUtil                     securityUtil;

    @Transactional(readOnly = true)
    @Override
    public Page<AssetAssignmentHistoryResponse> historyForAsset(Long assetId, Pageable pageable) {
        return historyRepository.findByAssetIdOrderByAssignedAtDesc(assetId, pageable)
            .map(AssetAssignmentHistoryMapper::toAssetHistoryResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<AssetAssignmentHistoryResponse> historyForEmployee(Long employeeId, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null) throw new BadRequestException("No company context");
        return historyRepository.findByCompanyIdAndEmployeeIdOrderByAssignedAtDesc(companyId, employeeId, pageable)
            .map(AssetAssignmentHistoryMapper::toAssetHistoryResponse);
    }

    @Override
    public Page<AssetAssignmentHistoryResponse> historyForAsset(Long assetId, java.awt.print.Pageable pageable) {
        return null;
    }

    @Override
    public Page<AssetAssignmentHistoryResponse> historyForEmployee(Long employeeId, java.awt.print.Pageable pageable) {
        return null;
    }
}

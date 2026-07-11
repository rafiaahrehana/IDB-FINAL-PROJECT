package com.businessos.modules.hrm.asset;

import com.businessos.modules.company.Company;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.enums.AssetStatus;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;

import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.security.SecurityUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor

public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final EmployeeRepository employeeRepository;
    private final AssetAssignmentHistoryRepository historyRepository;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public AssetResponse create(AssetRequest request) {
        Long companyId = requireCompanyId();
        Asset asset = new Asset(); asset.setName(request.getName()); asset.setCategory(request.getCategory()); asset.setSerialNumber(request.getSerialNumber()); asset.setNotes(request.getDescription()); asset.setPurchaseDate(request.getPurchaseDate()); asset.setPurchasePrice(request.getPurchaseCost() != null ? request.getPurchaseCost().doubleValue() : null); asset.setStatus(AssetStatus.AVAILABLE); asset.setCompany(companyRef(companyId));
        asset.setAssetTag(request.getAssetTag());
        asset.setBrand(request.getBrand());
        asset.setModel(request.getModel());
        asset.setIpAddress(request.getIpAddress());
        asset.setMacAddress(request.getMacAddress());
        asset.setProcessorModel(request.getProcessorModel());
        asset.setRamSize(request.getRamSize());
        asset.setStorageSize(request.getStorageSize());
        asset.setOperatingSystem(request.getOperatingSystem());
        asset.setWarrantyExpiry(request.getWarrantyExpiry());

        if (request.getAssignedToId() != null) {
            Employee emp = findEmployee(request.getAssignedToId(), companyId);
            asset.setAssignedTo(emp);
            asset.setStatus(AssetStatus.ASSIGNED);
            asset.setAssignedAt(LocalDate.now());
            assetRepository.save(asset);
            recordHistory(asset, emp, LocalDate.now(), null, null, companyId);
        } else {
            assetRepository.save(asset);
        }
        return AssetMapper.toAssetResponse(asset);
    }

    @Override
    @Transactional(readOnly = true)
    public AssetResponse getById(Long id) {
        return AssetMapper.toAssetResponse(findInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> listAll(AssetStatus status, Pageable pageable) {
        Long companyId = requireCompanyId();
        Page<Asset> page = status != null
            ? assetRepository.findByCompanyIdAndStatus(companyId, status, pageable)
            : assetRepository.findByCompanyId(companyId, pageable);
        return page.map(AssetMapper::toAssetResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetResponse> listForEmployee(Long employeeId) {
        return assetRepository.findByCompanyIdAndAssignedToId(requireCompanyId(), employeeId)
            .stream().map(AssetMapper::toAssetResponse).toList();
    }

    @Override
    @Transactional
    public AssetResponse update(Long id, AssetRequest request) {
        Asset asset = findInTenant(id);
        if (request.getName()         != null) asset.setName(request.getName());
        if (request.getCategory()     != null) asset.setCategory(request.getCategory());
        if (request.getSerialNumber() != null) asset.setSerialNumber(request.getSerialNumber());
        if (request.getDescription()  != null) asset.setNotes(request.getDescription());
        if (request.getPurchaseDate() != null) asset.setPurchaseDate(request.getPurchaseDate());
        if (request.getPurchaseCost() != null) asset.setPurchasePrice(request.getPurchaseCost() != null ? request.getPurchaseCost().doubleValue() : null);
        if (request.getNotes()        != null) asset.setNotes(request.getNotes());
        if (request.getAssetTag()        != null) asset.setAssetTag(request.getAssetTag());
        if (request.getBrand()           != null) asset.setBrand(request.getBrand());
        if (request.getModel()           != null) asset.setModel(request.getModel());
        if (request.getIpAddress()       != null) asset.setIpAddress(request.getIpAddress());
        if (request.getMacAddress()      != null) asset.setMacAddress(request.getMacAddress());
        if (request.getProcessorModel()  != null) asset.setProcessorModel(request.getProcessorModel());
        if (request.getRamSize()         != null) asset.setRamSize(request.getRamSize());
        if (request.getStorageSize()     != null) asset.setStorageSize(request.getStorageSize());
        if (request.getOperatingSystem() != null) asset.setOperatingSystem(request.getOperatingSystem());
        if (request.getWarrantyExpiry()  != null) asset.setWarrantyExpiry(request.getWarrantyExpiry());
        return AssetMapper.toAssetResponse(asset);
    }

    @Override
    @Transactional
    public AssetResponse assign(Long id, Long employeeId) {
        Long companyId = requireCompanyId();
        Asset asset = findInTenant(id);
        if (asset.getStatus() == AssetStatus.ASSIGNED) {
            throw new BadRequestException("Asset is already assigned. Unassign it first.");
        }
        Employee emp = findEmployee(employeeId, companyId);
        asset.setAssignedTo(emp);
        asset.setStatus(AssetStatus.ASSIGNED);
        asset.setAssignedAt(LocalDate.now());
        asset.setReturnedAt(null);
        recordHistory(asset, emp, LocalDate.now(), null, null, companyId);
        return AssetMapper.toAssetResponse(asset);
    }

    @Override
    @Transactional
    public AssetResponse unassign(Long id) {
        Long companyId = requireCompanyId();
        Asset asset = findInTenant(id);
        if (asset.getStatus() != AssetStatus.ASSIGNED) {
            throw new BadRequestException("Asset is not currently assigned");
        }
        // Close the open history record
        historyRepository.findTopByAssetIdAndReturnedAtIsNullOrderByAssignedAtDesc(id)
            .ifPresent(h -> {
                h.setReturnedAt(LocalDate.now());
                historyRepository.save(h);
            });
        asset.setAssignedTo(null);
        asset.setStatus(AssetStatus.AVAILABLE);
        asset.setReturnedAt(LocalDate.now());
        return AssetMapper.toAssetResponse(asset);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Asset asset = findInTenant(id);
        if (asset.getStatus() == AssetStatus.ASSIGNED) {
            throw new BadRequestException("Cannot delete an assigned asset. Unassign it first.");
        }
        asset.softDelete();
    }

    private void recordHistory(Asset asset, Employee emp, LocalDate assignedAt,
                                LocalDate returnedAt, String condition, Long companyId) {
        Company c = new Company(); c.setId(companyId);
        com.businessos.modules.itam.shared.AssetHistory history = com.businessos.modules.itam.shared.AssetHistory.builder()
            .asset(asset)
            .employee(emp)
            .company(c)
            .assignedAt(assignedAt)
            .returnedAt(returnedAt)
            .condition(condition)
            .assignedBy(securityUtil.getCurrentUser())
            .build();
        historyRepository.save(history);
    }

    private Asset findInTenant(Long id) {
        return assetRepository.findByIdAndCompanyId(id, requireCompanyId())
            .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + id));
    }

    private Employee findEmployee(Long employeeId, Long companyId) {
        return employeeRepository.findByIdAndCompanyId(employeeId, companyId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));
    }

    private Long requireCompanyId() {
        Long id = securityUtil.getCurrentCompanyId();
        if (id == null) throw new BadRequestException("No company context");
        return id;
    }

    private Company companyRef(Long companyId) {
        Company c = new Company(); c.setId(companyId); return c;
    }
}


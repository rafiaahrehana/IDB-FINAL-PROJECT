package com.businessos.modules.hrm.designation;

import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import com.businessos.modules.company.Company;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.security.SecurityUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor

public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;
    private final SecurityUtil securityUtil;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional
    public DesignationResponse create(DesignationRequest request) {
        authorizationService.checkPermission(PermissionCode.DESIGNATION_CREATE);
        Long companyId = requireCompanyId();
        if (designationRepository.existsByCompanyIdAndCode(companyId, request.getCode().toUpperCase())) {
            throw new BadRequestException("Designation code '" + request.getCode() + "' already exists");
        }
        Designation d = Designation.builder()
            .name(request.getName())
            .code(request.getCode().toUpperCase())
            .level(request.getLevel())
            .description(request.getDescription())
            .company(companyRef(companyId))
            .build();
        designationRepository.save(d);
        return DesignationMapper.toDesignationResponse(d);
    }

    @Override
    @Transactional(readOnly = true)
    public DesignationResponse getById(Long id) {
        return DesignationMapper.toDesignationResponse(findInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DesignationResponse> listAll(Pageable pageable) {
        // Deliberately NOT gated by DESIGNATION_VIEW: same reasoning as
        // DepartmentServiceImpl.listAll() - this endpoint doubles as a cross-module
        // picker. Frontend sidebar/route gating only, until the endpoint is split.
        return designationRepository.findByCompanyId(requireCompanyId(), pageable)
            .map(DesignationMapper::toDesignationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DesignationResponse> listActive() {
        return designationRepository.findByCompanyIdAndActiveTrueOrderByLevelAsc(requireCompanyId())
            .stream().map(DesignationMapper::toDesignationResponse).toList();
    }

    @Override
    @Transactional
    public DesignationResponse update(Long id, DesignationRequest request) {
        authorizationService.checkPermission(PermissionCode.DESIGNATION_UPDATE);
        Long companyId = requireCompanyId();
        Designation d = findInTenant(id);
        if (!d.getCode().equals(request.getCode().toUpperCase())
                && designationRepository.existsByCompanyIdAndCodeAndIdNot(
                    companyId, request.getCode().toUpperCase(), id)) {
            throw new BadRequestException("Designation code '" + request.getCode() + "' already exists");
        }
        d.setName(request.getName());
        d.setCode(request.getCode().toUpperCase());
        d.setLevel(request.getLevel());
        if (request.getDescription() != null) d.setDescription(request.getDescription());
        return DesignationMapper.toDesignationResponse(d);
    }

    @Override
    @Transactional
    public DesignationResponse toggleActive(Long id) {
        authorizationService.checkPermission(PermissionCode.DESIGNATION_UPDATE);
        Designation d = findInTenant(id);
        d.setActive(!d.isActive());
        return DesignationMapper.toDesignationResponse(d);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        authorizationService.checkPermission(PermissionCode.DESIGNATION_DELETE);
        findInTenant(id).softDelete();
    }

    private Designation findInTenant(Long id) {
        return designationRepository.findByIdAndCompanyId(id, requireCompanyId())
            .orElseThrow(() -> new ResourceNotFoundException("Designation not found: " + id));
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

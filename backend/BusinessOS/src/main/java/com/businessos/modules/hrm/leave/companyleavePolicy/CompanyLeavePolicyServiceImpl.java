package com.businessos.modules.hrm.leave.companyleavePolicy;

import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyLeavePolicy;
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

public class CompanyLeavePolicyServiceImpl implements CompanyLeavePolicyService {

    private final CompanyLeavePolicyRepository policyRepository;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public CompanyLeavePolicyResponse create(CompanyLeavePolicyRequest request) {
        Long companyId = requireCompanyId();
        CompanyLeavePolicy policy = CompanyLeavePolicy.builder()
            .leaveType(request.getLeaveType())
            .employmentType(request.getEmploymentType())
            .annualEntitlement(request.getAnnualEntitlement())
            .maxCarryForward(request.getMaxCarryForward() != null ? request.getMaxCarryForward() : 0)
            .maxConsecutiveDays(request.getMaxConsecutiveDays())
            .requiresApproval(request.isRequiresApproval())
            .canCarryForward(request.isCanCarryForward())
            .paid(request.isPaid())
            .applicableFromMonths(request.getApplicableFromMonths() != null ? request.getApplicableFromMonths() : 0)
            .company(companyRef(companyId))
            .build();
        policyRepository.save(policy);
        return CompanyLeavePolicyMapper.toLeavePolicyResponse(policy);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyLeavePolicyResponse getById(Long id) {
        return CompanyLeavePolicyMapper.toLeavePolicyResponse(findInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyLeavePolicyResponse> listAll(Pageable pageable) {
        return policyRepository.findByCompanyId(requireCompanyId(), pageable)
            .map(CompanyLeavePolicyMapper::toLeavePolicyResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyLeavePolicyResponse> listActive() {
        return policyRepository.findByCompanyIdAndActiveTrue(requireCompanyId())
            .stream().map(CompanyLeavePolicyMapper::toLeavePolicyResponse).toList();
    }

    @Override
    @Transactional
    public CompanyLeavePolicyResponse update(Long id, CompanyLeavePolicyRequest request) {
        CompanyLeavePolicy policy = findInTenant(id);
        policy.setLeaveType(request.getLeaveType());
        policy.setEmploymentType(request.getEmploymentType());
        policy.setAnnualEntitlement(request.getAnnualEntitlement());
        if (request.getMaxCarryForward()      != null) policy.setMaxCarryForward(request.getMaxCarryForward());
        if (request.getMaxConsecutiveDays()   != null) policy.setMaxConsecutiveDays(request.getMaxConsecutiveDays());
        policy.setRequiresApproval(request.isRequiresApproval());
        policy.setCanCarryForward(request.isCanCarryForward());
        policy.setPaid(request.isPaid());
        if (request.getApplicableFromMonths() != null) policy.setApplicableFromMonths(request.getApplicableFromMonths());
        return CompanyLeavePolicyMapper.toLeavePolicyResponse(policy);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findInTenant(id).softDelete();
    }

    private CompanyLeavePolicy findInTenant(Long id) {
        return policyRepository.findByIdAndCompanyId(id, requireCompanyId())
            .orElseThrow(() -> new ResourceNotFoundException("Leave policy not found: " + id));
    }

    private Long requireCompanyId() {
        Long cid = securityUtil.getCurrentCompanyId();
        if (cid == null) throw new BadRequestException("No company context");
        return cid;
    }

    private Company companyRef(Long companyId) {
        Company c = new Company(); c.setId(companyId); return c;
    }
}

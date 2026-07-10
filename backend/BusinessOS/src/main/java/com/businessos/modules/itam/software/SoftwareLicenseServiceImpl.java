package com.businessos.modules.itam.software;

import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SoftwareLicenseServiceImpl implements SoftwareLicenseService {

    private final SoftwareLicenseRepository licenseRepository;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public SoftwareLicenseResponse create(SoftwareLicenseRequest request) {
        Long companyId = securityUtil.getCurrentCompanyId();

        if (licenseRepository.findByCompanyIdAndLicenseKey(companyId, request.getLicenseKey()).isPresent()) {
            throw new BadRequestException("License key already exists: " + request.getLicenseKey());
        }

        SoftwareLicense license = SoftwareLicenseMapper.toEntity(request);
        license.setCompanyId(companyId);
        license.setLicenseStatus(LicenseStatus.ACTIVE);
        license.setSeatsAvailable(license.getTotalSeatsLicensed());

        license = licenseRepository.save(license);
        return SoftwareLicenseMapper.toResponse(license);
    }

    @Override
    @Transactional(readOnly = true)
    public SoftwareLicenseResponse getById(Long id) {
        SoftwareLicense license = licenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("License not found"));
        return SoftwareLicenseMapper.toResponse(license);
    }

    @Override
    @Transactional(readOnly = true)
    public SoftwareLicenseResponse getByLicenseKey(String key) {
        Long companyId = securityUtil.getCurrentCompanyId();
        SoftwareLicense license = licenseRepository.findByCompanyIdAndLicenseKey(companyId, key)
                .orElseThrow(() -> new ResourceNotFoundException("License not found"));
        return SoftwareLicenseMapper.toResponse(license);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SoftwareLicenseResponse> getAll(Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return licenseRepository.findByCompanyId(companyId, pageable)
                .map(SoftwareLicenseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SoftwareLicenseResponse> getByStatus(LicenseStatus status, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return licenseRepository.findByCompanyIdAndLicenseStatus(companyId, status, pageable)
                .map(SoftwareLicenseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SoftwareLicenseResponse> getExpiringLicenses() {
        Long companyId = securityUtil.getCurrentCompanyId();
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        return licenseRepository.findExpiringBetweenDates(companyId, LocalDate.now(), thirtyDaysFromNow)
                .stream()
                .map(SoftwareLicenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SoftwareLicenseResponse> getExpiredLicenses() {
        Long companyId = securityUtil.getCurrentCompanyId();
        return licenseRepository.findExpiredLicenses(companyId, LocalDate.now())
                .stream()
                .map(SoftwareLicenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SoftwareLicenseResponse update(Long id, SoftwareLicenseRequest request) {
        SoftwareLicense license = licenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("License not found"));

        license.setSoftwareName(request.getSoftwareName());
        license.setPublisher(request.getPublisher());
        license.setLicenseType(request.getLicenseType());
        license.setTotalSeatsLicensed(request.getTotalSeatsLicensed());
        license.setLicenseExpiryDate(request.getLicenseExpiryDate());
        license.setRenewalType(request.getRenewalType());
        license.setAutoRenew(request.isAutoRenew());
        license.setNotes(request.getNotes());

        license = licenseRepository.save(license);
        return SoftwareLicenseMapper.toResponse(license);
    }

    @Override
    @Transactional
    public void assignSeat(Long licenseId) {
        SoftwareLicense license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new ResourceNotFoundException("License not found"));

        if (license.getSeatsAvailable() <= 0) {
            throw new BadRequestException("No seats available");
        }
        license.setSeatsAvailable(license.getSeatsAvailable() - 1);
        licenseRepository.save(license);
    }

    @Override
    @Transactional
    public void releaseSeat(Long licenseId) {
        SoftwareLicense license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new ResourceNotFoundException("License not found"));

        license.setSeatsAvailable(license.getSeatsAvailable() + 1);
        licenseRepository.save(license);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SoftwareLicense license = licenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("License not found"));
        license.setDeleted(true);
        licenseRepository.save(license);
    }
}

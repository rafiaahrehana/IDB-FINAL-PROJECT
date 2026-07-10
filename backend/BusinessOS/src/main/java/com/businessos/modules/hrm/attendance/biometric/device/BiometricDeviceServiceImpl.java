package com.businessos.modules.hrm.attendance.biometric.device;

import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BiometricDeviceServiceImpl implements BiometricDeviceService {

    private final BiometricDeviceRepository deviceRepository;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public BiometricDeviceResponse create(BiometricDeviceRequest request) {
        Long companyId = securityUtil.getCurrentCompanyId();

        if (deviceRepository.findByCompanyIdAndDeviceId(companyId, request.getDeviceId()).isPresent()) {
            throw new BadRequestException("Device ID already exists: " + request.getDeviceId());
        }

        BiometricDevice device = BiometricDeviceMapper.toEntity(request);
        device.setCompanyId(companyId);
        device.setStatus(BiometricDeviceStatus.ACTIVE);
        device.setLastSyncTime(LocalDateTime.now());

        device = deviceRepository.save(device);
        return BiometricDeviceMapper.toResponse(device);
    }

    @Override
    @Transactional(readOnly = true)
    public BiometricDeviceResponse getById(Long id) {
        BiometricDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
        return BiometricDeviceMapper.toResponse(device);
    }

    @Override
    @Transactional(readOnly = true)
    public BiometricDeviceResponse getByDeviceId(String deviceId) {
        Long companyId = securityUtil.getCurrentCompanyId();
        BiometricDevice device = deviceRepository.findByCompanyIdAndDeviceId(companyId, deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
        return BiometricDeviceMapper.toResponse(device);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BiometricDeviceResponse> getAll(Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return deviceRepository.findByCompanyId(companyId, pageable)
                .map(BiometricDeviceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BiometricDeviceResponse> getByStatus(BiometricDeviceStatus status, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return deviceRepository.findByCompanyIdAndStatus(companyId, status, pageable)
                .map(BiometricDeviceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BiometricDeviceResponse> getOnlineDevices() {
        Long companyId = securityUtil.getCurrentCompanyId();
        return deviceRepository.findByCompanyIdAndIsOnline(companyId, true)
                .stream()
                .map(BiometricDeviceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BiometricDeviceResponse update(Long id, BiometricDeviceRequest request) {
        BiometricDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        device.setDeviceName(request.getDeviceName());
        device.setDeviceType(request.getDeviceType());
        device.setIpAddress(request.getIpAddress());
        device.setPortNumber(request.getPortNumber());
        device.setLocation(request.getLocation());
        device.setDepartment(request.getDepartment());
        if (request.getMatchThreshold() != null) {
            device.setMatchThreshold(request.getMatchThreshold());
        }
        if (request.getEnabledForCheckIn() != null) {
            device.setEnabledForCheckIn(request.getEnabledForCheckIn());
        }
        if (request.getEnabledForCheckOut() != null) {
            device.setEnabledForCheckOut(request.getEnabledForCheckOut());
        }
        device.setNotes(request.getNotes());

        device = deviceRepository.save(device);
        return BiometricDeviceMapper.toResponse(device);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, BiometricDeviceStatus status) {
        BiometricDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
        device.setStatus(status);
        deviceRepository.save(device);
    }

    @Override
    @Transactional
    public void updateOnlineStatus(Long id, boolean online) {
        BiometricDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
        device.setOnline(online);
        device.setLastHealthCheckTime(LocalDateTime.now());
        deviceRepository.save(device);
    }

    @Override
    @Transactional
    public void recordSync(Long id) {
        BiometricDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
        device.setLastSyncTime(LocalDateTime.now());
        deviceRepository.save(device);
    }

    @Override
    @Transactional
    public BiometricDeviceResponse delete(Long id) {
        BiometricDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
        device.softDelete();
        deviceRepository.save(device);
        return BiometricDeviceMapper.toResponse(device);
    }
}



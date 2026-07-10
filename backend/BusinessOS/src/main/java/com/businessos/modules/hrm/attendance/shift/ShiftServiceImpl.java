package com.businessos.modules.hrm.attendance.shift;

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

public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final SecurityUtil    securityUtil;

    @Override
    @Transactional
    public ShiftResponse create(ShiftRequest request) {
        Long companyId = requireCompanyId();
        if (shiftRepository.existsByCompanyIdAndName(companyId, request.getName())) {
            throw new BadRequestException("Shift '" + request.getName() + "' already exists");
        }
        Shift s = Shift.builder()
            .name(request.getName())
            .shiftType(request.getShiftType())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .gracePeriodMinutes(request.getGracePeriodMinutes() != null ? request.getGracePeriodMinutes() : 10)
            .weeklyOffDays(request.getWeeklyOffDays() != null ? request.getWeeklyOffDays() : "SAT,SUN")
            .flexible(request.isFlexible())
            .nightShift(request.isNightShift())
            .workingMinutes(request.getStartTime() != null && request.getEndTime() != null ? java.time.temporal.ChronoUnit.MINUTES.between(request.getStartTime(), request.getEndTime()) : 0)
            .description(request.getDescription())
            .notes(request.getNotes())
            .company(companyRef(companyId))
            .build();
        shiftRepository.save(s);
        return ShiftMapper.toShiftResponse(s);
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftResponse getById(Long id) {
        return ShiftMapper.toShiftResponse(findInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShiftResponse> listAll(Pageable pageable) {
        return shiftRepository.findByCompanyId(requireCompanyId(), pageable)
            .map(ShiftMapper::toShiftResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftResponse> listActive() {
        return shiftRepository.findByCompanyIdAndActiveTrue(requireCompanyId())
            .stream().map(ShiftMapper::toShiftResponse).toList();
    }

    @Override
    @Transactional
    public ShiftResponse update(Long id, ShiftRequest request) {
        Shift s = findInTenant(id);
        s.setName(request.getName());
        s.setShiftType(request.getShiftType());
        s.setStartTime(request.getStartTime());
        s.setEndTime(request.getEndTime());
        if (request.getGracePeriodMinutes() != null) s.setGracePeriodMinutes(request.getGracePeriodMinutes());
        if (request.getWeeklyOffDays() != null) s.setWeeklyOffDays(request.getWeeklyOffDays());
        s.setFlexible(request.isFlexible());
        s.setNightShift(request.isNightShift());
        s.setWorkingMinutes(request.getStartTime() != null && request.getEndTime() != null ? java.time.temporal.ChronoUnit.MINUTES.between(request.getStartTime(), request.getEndTime()) : 0);
        s.setDescription(request.getDescription());
        s.setNotes(request.getNotes());
        return ShiftMapper.toShiftResponse(s);
    }

    @Override
    @Transactional
    public ShiftResponse toggleActive(Long id) {
        Shift s = findInTenant(id);
        s.setActive(!s.isActive());
        return ShiftMapper.toShiftResponse(s);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findInTenant(id).softDelete();
    }

    private Shift findInTenant(Long id) {
        return shiftRepository.findByIdAndCompanyId(id, requireCompanyId())
            .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + id));
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

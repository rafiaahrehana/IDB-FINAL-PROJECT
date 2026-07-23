package com.businessos.modules.hrm.leave.holiday;

import com.businessos.modules.company.Company;
import com.businessos.modules.hrm.department.Department;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.modules.hrm.department.DepartmentRepository;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
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

public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;
    private final DepartmentRepository departmentRepository;
    private final SecurityUtil         securityUtil;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional
    public HolidayResponse create(HolidayRequest request) {
        authorizationService.checkPermission(PermissionCode.HOLIDAY_CREATE);
        Long companyId = requireCompanyId();
        if (holidayRepository.existsByCompanyIdAndDate(companyId, request.getHolidayDate())) {
            throw new BadRequestException("A holiday already exists on " + request.getHolidayDate());
        }

        Holiday holiday = new Holiday(); holiday.setName(request.getName()); holiday.setDate(request.getHolidayDate()); holiday.setType(request.getHolidayType()); holiday.setDescription(request.getDescription()); holiday.setCompany(companyRef(companyId));

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findByIdAndCompanyId(request.getDepartmentId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));
            // holiday.setDepartment(dept);
        }

        holidayRepository.save(holiday);
        return HolidayMapper.toHolidayResponse(holiday);
    }

    @Override
    @Transactional(readOnly = true)
    public HolidayResponse getById(Long id) {
        return HolidayMapper.toHolidayResponse(findInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HolidayResponse> listAll(Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.HOLIDAY_VIEW);
        return holidayRepository.findByCompanyId(requireCompanyId(), pageable)
            .map(HolidayMapper::toHolidayResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayResponse> listByYear(int year) {
        authorizationService.checkPermission(PermissionCode.HOLIDAY_VIEW);
        Long companyId = requireCompanyId();
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to   = LocalDate.of(year, 12, 31);
        return holidayRepository.findByCompanyAndDateRange(companyId, from, to)
            .stream().map(HolidayMapper::toHolidayResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayResponse> listByRange(LocalDate from, LocalDate to, Long departmentId) {
        authorizationService.checkPermission(PermissionCode.HOLIDAY_VIEW);
        Long companyId = requireCompanyId();
        List<Holiday> holidays = holidayRepository.findByCompanyAndDateRange(companyId, from, to);
        return holidays.stream().map(HolidayMapper::toHolidayResponse).toList();
    }

    @Override
    @Transactional
    public HolidayResponse update(Long id, HolidayRequest request) {
        authorizationService.checkPermission(PermissionCode.HOLIDAY_UPDATE);
        Long companyId = requireCompanyId();
        Holiday holiday = findInTenant(id);
        holiday.setName(request.getName());
        holiday.setDate(request.getHolidayDate());
        holiday.setType(request.getHolidayType());
        if (request.getDescription() != null) holiday.setDescription(request.getDescription());
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findByIdAndCompanyId(request.getDepartmentId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));
            // holiday.setDepartment(dept);
        } else {
            // holiday.setDepartment(null);
        }
        return HolidayMapper.toHolidayResponse(holiday);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        authorizationService.checkPermission(PermissionCode.HOLIDAY_DELETE);
        findInTenant(id).softDelete();
    }

    private Holiday findInTenant(Long id) {
        return holidayRepository.findByIdAndCompanyId(id, requireCompanyId())
            .orElseThrow(() -> new ResourceNotFoundException("Holiday not found: " + id));
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


package com.businessos.modules.project.meeting;

import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.auth.user.User;
import com.businessos.auth.user.UserRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    private Long requireCompanyId() {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null) throw new BadRequestException("No company context found.");
        return companyId;
    }

    @Transactional
    public MeetingResponse create(MeetingRequest request) {
        Long companyId = requireCompanyId();
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new BadRequestException("Company not found"));

        Meeting meeting = Meeting.builder()
            .company(company)
            .title(request.getTitle())
            .description(request.getDescription())
            .organizer(resolveUser(request.getOrganizerId()))
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .location(request.getLocation())
            .build();
        return toDTO(meetingRepository.save(meeting));
    }

    public Page<MeetingResponse> list(Pageable pageable) {
        return meetingRepository.findByCompanyId(requireCompanyId(), pageable).map(this::toDTO);
    }

    public MeetingResponse getById(Long id) {
        return toDTO(meetingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting not found: " + id)));
    }

    @Transactional
    public MeetingResponse update(Long id, MeetingRequest request) {
        Meeting meeting = meetingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting not found: " + id));
        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        meeting.setOrganizer(resolveUser(request.getOrganizerId()));
        meeting.setStartTime(request.getStartTime());
        meeting.setEndTime(request.getEndTime());
        meeting.setLocation(request.getLocation());
        return toDTO(meetingRepository.save(meeting));
    }

    @Transactional
    public void delete(Long id) {
        Meeting meeting = meetingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting not found: " + id));
        meeting.softDelete();
        meetingRepository.save(meeting);
    }

    public long getTodayCount() {
        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return meetingRepository.countByCompanyIdAndDay(requireCompanyId(), start, end);
    }

    public long getTotalCount() {
        return meetingRepository.countByCompanyId(requireCompanyId());
    }

    private MeetingResponse toDTO(Meeting m) {
        MeetingResponse r = new MeetingResponse();
        r.setId(m.getId());
        r.setTitle(m.getTitle());
        r.setDescription(m.getDescription());
        r.setOrganizerId(m.getOrganizer() != null ? m.getOrganizer().getId() : null);
        r.setStartTime(m.getStartTime());
        r.setEndTime(m.getEndTime());
        r.setLocation(m.getLocation());
        return r;
    }

    private User resolveUser(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}

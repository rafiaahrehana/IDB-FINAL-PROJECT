package com.businessos.modules.support.contextswitch;

import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.auth.user.User;
import com.businessos.auth.user.UserRepository;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportContextSwitchServiceImpl implements SupportContextSwitchService {

    private final SupportContextSwitchRepository contextSwitchRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public SupportContextSwitchResponse switchContext(SupportContextSwitchRequest request) {
        User user = userRepository.findById(request.getSupportAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Company company = companyRepository.findById(request.getViewedCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        // End any active switches
        Optional<SupportContextSwitch> activeSwitchOpt = contextSwitchRepository
                .findBySupportAgentIdAndStillActiveTrue(request.getSupportAgentId());

        activeSwitchOpt.ifPresent(activeSwitch -> {
            activeSwitch.setSwitchedOutTime(LocalDateTime.now());
            activeSwitch.setStillActive(false);
            contextSwitchRepository.save(activeSwitch);
        });

        // Create new context switch
        SupportContextSwitch contextSwitch = SupportContextSwitch.builder()
                .supportAgent(user)
                .viewedCompany(company)
                .switchedInTime(LocalDateTime.now())
                .purpose(request.getPurpose())
                .ipAddress(request.getIpAddress())
                .stillActive(true)
                .build();

        contextSwitch = contextSwitchRepository.save(contextSwitch);
        return SupportContextSwitchMapper.toResponse(contextSwitch);
    }

    @Override
    @Transactional
    public void endContextSwitch(Long contextSwitchId) {
        SupportContextSwitch contextSwitch = contextSwitchRepository.findById(contextSwitchId)
                .orElseThrow(() -> new ResourceNotFoundException("Context switch not found"));

        contextSwitch.setSwitchedOutTime(LocalDateTime.now());
        contextSwitch.setStillActive(false);
        contextSwitchRepository.save(contextSwitch);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportContextSwitchResponse getActiveContextSwitch(Long supportAgentId) {
        SupportContextSwitch contextSwitch = contextSwitchRepository
                .findBySupportAgentIdAndStillActiveTrue(supportAgentId)
                .orElseThrow(() -> new ResourceNotFoundException("No active context switch found"));

        return SupportContextSwitchMapper.toResponse(contextSwitch);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportContextSwitchResponse> getContextSwitchHistory(Long supportAgentId, Pageable pageable) {
        return contextSwitchRepository.findBySupportAgentId(supportAgentId, pageable)
                .map(SupportContextSwitchMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportContextSwitchResponse> getActiveContextSwitches() {
        return contextSwitchRepository.findByStillActiveTrue()
                .stream()
                .map(SupportContextSwitchMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SupportContextSwitchResponse getById(Long id) {
        SupportContextSwitch contextSwitch = contextSwitchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Context switch not found"));
        return SupportContextSwitchMapper.toResponse(contextSwitch);
    }
}


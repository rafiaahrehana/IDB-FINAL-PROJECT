package com.businessos.modules.support.contextswitch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SupportContextSwitchService {
    SupportContextSwitchResponse switchContext(SupportContextSwitchRequest request);
    void endContextSwitch(Long contextSwitchId);
    SupportContextSwitchResponse getActiveContextSwitch(Long supportAgentId);
    Page<SupportContextSwitchResponse> getContextSwitchHistory(Long supportAgentId, Pageable pageable);
    List<SupportContextSwitchResponse> getActiveContextSwitches();
    SupportContextSwitchResponse getById(Long id);
}

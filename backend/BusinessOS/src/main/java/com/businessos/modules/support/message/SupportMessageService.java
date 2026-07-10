package com.businessos.modules.support.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SupportMessageService {
    SupportMessageResponse create(SupportMessageRequest request);
    SupportMessageResponse getById(Long id);
    Page<SupportMessageResponse> getByTicket(Long ticketId, Pageable pageable);
    List<SupportMessageResponse> getExternalMessages(Long ticketId);
    List<SupportMessageResponse> getInternalNotes(Long ticketId);
    SupportMessageResponse update(Long id, SupportMessageRequest request);
    SupportMessageResponse delete(Long id);
}

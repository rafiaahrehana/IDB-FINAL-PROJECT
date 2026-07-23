package com.businessos.modules.support.message;

import com.businessos.auth.user.User;
import com.businessos.auth.user.UserRepository;
import com.businessos.modules.support.ticket.SupportTicket;
import com.businessos.modules.support.ticket.SupportTicketRepository;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportMessageServiceImpl implements SupportMessageService {

    private final SupportMessageRepository messageRepository;
    private final SupportTicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional
    public SupportMessageResponse create(SupportMessageRequest request) {
        SupportTicket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        User sentBy = userRepository.findById(request.getSentByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SupportMessage message = SupportMessage.builder()
                .ticket(ticket)
                .sentBy(sentBy)
                .message(request.getMessage())
                .messageType(request.getMessageType())
                .isInternal(request.isInternal())
                .attachmentUrl(request.getAttachmentUrl())
                .isResolution(request.isResolution())
                .build();

        message = messageRepository.save(message);
        return SupportMessageMapper.toResponse(message);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportMessageResponse getById(Long id) {
        SupportMessage message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        return SupportMessageMapper.toResponse(message);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportMessageResponse> getByTicket(Long ticketId, Pageable pageable) {
        checkTenantPermission();
        return messageRepository.findByTicketId(ticketId, pageable)
                .map(SupportMessageMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportMessageResponse> getExternalMessages(Long ticketId) {
        checkTenantPermission();
        return messageRepository.findByTicketIdAndIsInternalFalse(ticketId)
                .stream()
                .map(SupportMessageMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportMessageResponse> getInternalNotes(Long ticketId) {
        checkTenantPermission();
        return messageRepository.findByTicketIdAndIsInternalTrue(ticketId)
                .stream()
                .map(SupportMessageMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SupportMessageResponse update(Long id, SupportMessageRequest request) {
        SupportMessage message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        message.setMessage(request.getMessage());
        message.setAttachmentUrl(request.getAttachmentUrl());

        message = messageRepository.save(message);
        return SupportMessageMapper.toResponse(message);
    }

    @Override
    @Transactional
    public SupportMessageResponse delete(Long id) {
        SupportMessage message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        message.softDelete();
        messageRepository.save(message);
        return SupportMessageMapper.toResponse(message);
    }

    // Both endpoints also allow SUPPORT_AGENT/SUPPORT_MANAGER (platform staff with no
    // CustomRole) per their @PreAuthorize - only gate the tenant caller branch here.
    private void checkTenantPermission() {
        com.businessos.auth.user.User current = securityUtil.getCurrentUser();
        if (current != null && !current.isPlatformUser()) {
            authorizationService.checkPermission(PermissionCode.SUPPORT_MESSAGE_VIEW);
        }
    }
}

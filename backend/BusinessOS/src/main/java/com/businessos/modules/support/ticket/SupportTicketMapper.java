package com.businessos.modules.support.ticket;

public class SupportTicketMapper {

    public static SupportTicketResponse toResponse(SupportTicket entity) {
        if (entity == null) return null;

        return SupportTicketResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .ticketNumber(entity.getTicketNumber())
                .createdByName(entity.getCreatedBy() != null ? entity.getCreatedBy().getFullName() : null)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .categoryName(entity.getCategory() != null ? entity.getCategory().getCategoryName() : null)
                .status(entity.getStatus())
                .priority(entity.getPriority())
                .source(entity.getSource())
                .assignedToAgentName(entity.getAssignedToAgent() != null ?
                        entity.getAssignedToAgent().getUser().getFullName() : null)
                .assignedDate(entity.getAssignedDate())
                .firstResponseTime(entity.getFirstResponseTime())
                .firstResponseDeadline(entity.getFirstResponseDeadline())
                .resolutionTime(entity.getResolutionTime())
                .resolutionDeadline(entity.getResolutionDeadline())
                .slaBreached(entity.isSlaBreached())
                .escalationLevel(entity.getEscalationLevel())
                .satisfactionRating(entity.getSatisfactionRating())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static SupportTicket toEntity(SupportTicketRequest request) {
        if (request == null) return null;

        return SupportTicket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .source(request.getSource())
                .build();
    }
}
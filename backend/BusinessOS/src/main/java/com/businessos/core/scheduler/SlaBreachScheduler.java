package com.businessos.core.scheduler;

import com.businessos.enums.NotificationType;
import com.businessos.enums.ServiceRequestStatus;
import com.businessos.modules.servicedesk.servicerequest.ServiceRequest;
import com.businessos.modules.servicedesk.servicerequest.ServiceRequestRepository;
import com.businessos.shared.notification.CreateNotificationRequest;
import com.businessos.shared.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor

public class SlaBreachScheduler {

    private final ServiceRequestRepository serviceRequestRepository;
    private final NotificationService notificationService;

    private static final List<ServiceRequestStatus> CLOSED_STATUSES = List.of(
            ServiceRequestStatus.COMPLETED,
            ServiceRequestStatus.CANCELLED,
            ServiceRequestStatus.REJECTED
    );

    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void markSlaBreaches() {
        LocalDateTime now = LocalDateTime.now();

        List<ServiceRequest> newlyBreached =
                serviceRequestRepository.findNewlyBreached(now, CLOSED_STATUSES);

        serviceRequestRepository.bulkMarkSlaBreaches(now, CLOSED_STATUSES);

        for (ServiceRequest request : newlyBreached) {
            if (request.getAssignedEmployee() == null
                    || request.getAssignedEmployee().getUser() == null) {
                continue;
            }
            notificationService.sendForServiceRequest(CreateNotificationRequest.forRequest(
                    NotificationType.SLA_BREACHED,
                    "SLA breached",
                    "Service request \"" + request.getTitle() + "\" has passed its SLA deadline",
                    request.getAssignedEmployee().getUser().getId(),
                    request.getCompany().getId(),
                    request.getId()
            ));
        }
    }
}

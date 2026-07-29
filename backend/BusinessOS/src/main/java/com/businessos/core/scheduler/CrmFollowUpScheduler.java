package com.businessos.core.scheduler;

import com.businessos.enums.NotificationType;
import com.businessos.modules.crm.activity.CrmActivity;
import com.businessos.modules.crm.activity.CrmActivityRepository;
import com.businessos.shared.notification.CreateNotificationRequest;
import com.businessos.shared.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Wires up CrmActivity.followUpAt/followUpDone, which were previously persisted but
 * never read anywhere. Does not auto-mark followUpDone - the rep completes it manually
 * (e.g. from the Dashboard's Upcoming Follow-ups list), this only notifies once it's due.
 */
@Component
@RequiredArgsConstructor
public class CrmFollowUpScheduler {

    private final CrmActivityRepository crmActivityRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void notifyDueFollowUps() {
        LocalDateTime now = LocalDateTime.now();
        List<CrmActivity> due =
                crmActivityRepository.findByFollowUpAtLessThanEqualAndFollowUpDoneFalseAndDeletedFalse(now);

        for (CrmActivity activity : due) {
            if (activity.getPerformedBy() == null) continue;

            String subject = activity.getLead() != null ? activity.getLead().getContactName()
                    : activity.getOpportunity() != null ? activity.getOpportunity().getName()
                    : activity.getClient() != null ? activity.getClient().getClientCompanyName()
                    : activity.getSubject();

            notificationService.send(CreateNotificationRequest.of(
                    NotificationType.FOLLOW_UP_DUE,
                    "Follow-up due",
                    "Follow-up \"" + activity.getSubject() + "\" for " + subject + " is due.",
                    "/crm/leads",
                    activity.getPerformedBy().getId(),
                    activity.getCompany().getId()
            ));
        }
    }
}

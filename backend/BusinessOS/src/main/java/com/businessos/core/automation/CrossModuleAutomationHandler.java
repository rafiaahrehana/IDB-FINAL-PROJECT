package com.businessos.core.automation;

import com.businessos.enums.NotificationType;
import com.businessos.modules.crm.client.Client;
import com.businessos.modules.crm.client.ClientRepository;
import com.businessos.modules.servicedesk.billing.UsageBillingService;
import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.shared.notification.CreateNotificationRequest;
import com.businessos.shared.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrossModuleAutomationHandler {

    private final UsageBillingService usageBillingService;
    private final NotificationService notificationService;
    private final CompanyRepository companyRepository;
    private final ClientRepository clientRepository;

    @Async
    @EventListener
    public void onServiceRequestCompleted(ServiceRequestCompletedEvent event) {
        try {
            usageBillingService.handleCompletion(event.getServiceRequestId());
        } catch (Exception ex) {
            log.error("[Automation] UsageBilling failed for request {}: {}",
                event.getServiceRequestId(), ex.getMessage(), ex);
        }
    }

    @Async
    @EventListener
    public void onOpportunityWon(OpportunityWonEvent event) {
        try {
            log.info("[Automation] Opportunity WON: id={} client={} company={} name='{}'",
                event.getOpportunityId(), event.getClientId(),
                event.getCompanyId(), event.getOpportunityName());

            Company company = companyRepository.findById(event.getCompanyId()).orElse(null);
            if (company == null || company.getOwner() == null) return;

            notificationService.send(CreateNotificationRequest.of(
                NotificationType.GENERAL,
                "Deal Won! 🎉",
                "Opportunity '" + event.getOpportunityName() + "' has been closed as WON.",
                "/crm/pipeline",
                company.getOwner().getId(),
                event.getCompanyId()));
        } catch (Exception ex) {
            log.error("[Automation] OpportunityWon handler failed: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @EventListener
    public void onInvoicePaid(InvoicePaidEvent event) {
        try {
            log.info("[Automation] Invoice PAID: id={} client={} company={} amount={}",
                event.getInvoiceId(), event.getClientId(),
                event.getCompanyId(), event.getAmount());

            Client client = clientRepository.findById(event.getClientId()).orElse(null);
            if (client == null || client.getUser() == null) return;

            notificationService.send(CreateNotificationRequest.of(
                NotificationType.PAYMENT_RECEIVED,
                "Payment Received",
                "Payment of " + event.getAmount() + " BDT received against invoice #" + event.getInvoiceId(),
                "/finance/invoices",
                client.getUser().getId(),
                event.getCompanyId()));

            Company company = companyRepository.findById(event.getCompanyId()).orElse(null);
            if (company != null && company.getOwner() != null
                    && !company.getOwner().getId().equals(client.getUser().getId())) {
                notificationService.send(CreateNotificationRequest.of(
                    NotificationType.PAYMENT_RECEIVED,
                    "Client Payment Received",
                    client.getClientCompanyName() + " paid " + event.getAmount() + " BDT for invoice #" + event.getInvoiceId(),
                    "/finance/invoices",
                    company.getOwner().getId(),
                    event.getCompanyId()));
            }
        } catch (Exception ex) {
            log.error("[Automation] InvoicePaid handler failed: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @EventListener
    public void onClientCreated(ClientCreatedEvent event) {
        try {
            log.info("[Automation] Client CREATED: id={} name={} email={} company={}",
                event.getClientId(), event.getClientName(),
                event.getClientEmail(), event.getCompanyId());

            Company company = companyRepository.findById(event.getCompanyId()).orElse(null);
            if (company == null || company.getOwner() == null) return;

            notificationService.send(CreateNotificationRequest.of(
                NotificationType.WELCOME,
                "New Client Added",
                "'" + event.getClientName() + "' (" + event.getClientEmail() + ") has been added as a client.",
                "/crm/clients/" + event.getClientId(),
                company.getOwner().getId(),
                event.getCompanyId()));
        } catch (Exception ex) {
            log.error("[Automation] ClientCreated handler failed: {}", ex.getMessage(), ex);
        }
    }
}

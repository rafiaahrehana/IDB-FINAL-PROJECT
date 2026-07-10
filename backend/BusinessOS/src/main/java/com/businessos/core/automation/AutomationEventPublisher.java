package com.businessos.core.automation;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AutomationEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publishOpportunityWon(Object source, Long companyId,
                                       Long opportunityId, Long clientId,
                                       String opportunityName) {
        publisher.publishEvent(
            new OpportunityWonEvent(source, companyId, opportunityId, clientId, opportunityName));
    }

    public void publishServiceRequestCompleted(Object source, Long companyId,
                                                Long serviceRequestId, Long clientId) {
        publisher.publishEvent(
            new ServiceRequestCompletedEvent(source, companyId, serviceRequestId, clientId));
    }

    public void publishInvoicePaid(Object source, Long companyId,
                                   Long invoiceId, Long clientId, BigDecimal amount) {
        publisher.publishEvent(
            new InvoicePaidEvent(source, companyId, invoiceId, clientId, amount));
    }

    public void publishClientCreated(Object source, Long companyId,
                                     Long clientId, String clientName, String clientEmail) {
        publisher.publishEvent(
            new ClientCreatedEvent(source, companyId, clientId, clientName, clientEmail));
    }
}

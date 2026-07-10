package com.businessos.core.automation;

import lombok.Getter;

@Getter
public class ClientCreatedEvent extends BusinessEvent {

    private final Long clientId;
    private final String clientName;
    private final String clientEmail;

    public ClientCreatedEvent(Object source, Long companyId,
                              Long clientId, String clientName, String clientEmail) {
        super(source, companyId, "CLIENT_CREATED");
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientEmail = clientEmail;
    }
}

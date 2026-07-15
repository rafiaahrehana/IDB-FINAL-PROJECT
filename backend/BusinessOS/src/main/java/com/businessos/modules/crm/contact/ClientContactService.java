package com.businessos.modules.crm.contact;

import java.util.List;

public interface ClientContactService {

    ClientContactResponse create(Long clientId, ClientContactRequest request);

    List<ClientContactResponse> listByClient(Long clientId);

    ClientContactResponse getById(Long clientId, Long id);

    ClientContactResponse update(Long clientId, Long id, ClientContactRequest request);

    ClientContactResponse markPrimary(Long clientId, Long id);

    void delete(Long clientId, Long id);
}

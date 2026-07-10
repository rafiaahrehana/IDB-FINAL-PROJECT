package com.businessos.modules.crm.contact;

import java.util.List;

public interface ClientContactService {

    ClientContactResponse create(Long clientId, ClientContactRequest request);

    List<ClientContactResponse> listByClient(Long clientId);

    ClientContactResponse getById(Long id);

    ClientContactResponse update(Long id, ClientContactRequest request);

    ClientContactResponse markPrimary(Long id);

    void delete(Long id);
}

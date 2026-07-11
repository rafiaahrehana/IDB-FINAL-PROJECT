package com.businessos.modules.crm.client;

import com.businessos.enums.ClientStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {

    ClientResponse create(CreateClientRequest request);

    ClientResponse getById(Long id);

    ClientResponse getMyProfile();

    Page<ClientResponse> listAll(ClientStatus status, Pageable pageable);

      ClientResponse update(Long id, UpdateClientRequest request);

    void delete(Long id);

    long getClientCount();

    boolean isClient(Long userId);
}

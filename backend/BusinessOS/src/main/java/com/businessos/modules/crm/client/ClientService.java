package com.businessos.modules.crm.client;

import com.businessos.enums.ClientStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClientService {

    ClientResponse create(CreateClientRequest request);

    /** Public, unauthenticated self-registration - the client picks their own company. */
    ClientResponse registerPublic(PublicClientRegisterRequest request);

    ClientResponse getById(Long id);

    ClientResponse getMyProfile();

    ClientResponse updateMyProfile(UpdateMyClientProfileRequest request);

    Page<ClientResponse> listAll(ClientStatus status, Pageable pageable);

    /** Lightweight, ungated - the active-client picker used by Invoices, Payment Receipts, and Pipeline. */
    List<ClientResponse> listActive();

      ClientResponse update(Long id, UpdateClientRequest request);

    void delete(Long id);

    long getClientCount();

    boolean isClient(Long userId);
}

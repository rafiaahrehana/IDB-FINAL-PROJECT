package com.businessos.modules.crm.contact;

import com.businessos.modules.crm.client.Client;
import com.businessos.modules.crm.client.ClientRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientContactServiceImpl implements ClientContactService {

    private final ClientContactRepository clientContactRepository;
    private final ClientRepository clientRepository;
    private final SecurityUtil securityUtil;

    @Override
    public ClientContactResponse create(Long clientId, ClientContactRequest request) {
        Long companyId = requireCompanyId();
        Client client = clientRepository.findByIdAndCompanyId(clientId, companyId)
            .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        if (request.getEmail() != null && !request.getEmail().isBlank()
            && clientContactRepository.existsByEmailAndClientIdAndCompanyIdAndDeletedFalse(request.getEmail(), clientId, companyId)) {
            throw new BadRequestException("A contact with this email already exists for this client");
        }

        boolean primary = Boolean.TRUE.equals(request.getPrimaryContact());
        if (primary) {
            clientContactRepository.clearPrimaryContact(clientId, companyId);
        }

        ClientContact contact = ClientContact.builder()
            .fullName(request.getFullName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .jobTitle(request.getJobTitle())
            .department(request.getDepartment())
            .primaryContact(primary)
            .notes(request.getNotes())
            .client(client)
            .company(client.getCompany())
            .build();

        return ClientContactMapper.toResponse(clientContactRepository.save(contact));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientContactResponse> listByClient(Long clientId) {
        Long companyId = requireCompanyId();
        clientRepository.findByIdAndCompanyId(clientId, companyId)
            .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        return clientContactRepository
            .findByClientIdAndCompanyIdOrderByPrimaryContactDescCreatedAtDesc(clientId, companyId)
            .stream()
            .map(ClientContactMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClientContactResponse getById(Long id) {
        return ClientContactMapper.toResponse(findOwned(id));
    }

    @Override
    public ClientContactResponse update(Long id, ClientContactRequest request) {
        ClientContact contact = findOwned(id);

        if (request.getFullName() != null) contact.setFullName(request.getFullName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setJobTitle(request.getJobTitle());
        contact.setDepartment(request.getDepartment());
        contact.setNotes(request.getNotes());

        if (Boolean.TRUE.equals(request.getPrimaryContact()) && !contact.isPrimaryContact()) {
            clientContactRepository.clearPrimaryContact(contact.getClient().getId(), requireCompanyId());
            contact.setPrimaryContact(true);
        }

        return ClientContactMapper.toResponse(clientContactRepository.save(contact));
    }

    @Override
    public ClientContactResponse markPrimary(Long id) {
        ClientContact contact = findOwned(id);
        clientContactRepository.clearPrimaryContact(contact.getClient().getId(), requireCompanyId());
        contact.setPrimaryContact(true);
        return ClientContactMapper.toResponse(clientContactRepository.save(contact));
    }

    @Override
    public void delete(Long id) {
        ClientContact contact = findOwned(id);
        contact.softDelete();
        clientContactRepository.save(contact);
    }

    private ClientContact findOwned(Long id) {
        return clientContactRepository.findByIdAndCompanyId(id, requireCompanyId())
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
    }

    private Long requireCompanyId() {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null) {
            throw new BadRequestException("No company context for current platformuser");
        }
        return companyId;
    }
}

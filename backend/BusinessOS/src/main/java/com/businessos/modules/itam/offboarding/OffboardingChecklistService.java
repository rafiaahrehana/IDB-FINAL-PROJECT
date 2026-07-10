package com.businessos.modules.itam.offboarding;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OffboardingChecklistService {

    OffboardingChecklistResponse create(OffboardingChecklistRequest request);

    OffboardingChecklistResponse getById(Long id);

    OffboardingChecklistResponse getByEmployee(Long employeeId);

    Page<OffboardingChecklistResponse> getAll(Pageable pageable);

    List<OffboardingChecklistResponse> getPendingChecklists();

    void markHardwareCollected(Long id);

    void markLicensesRevoked(Long id);

    void markAccessRevoked(Long id);

    void markDataHandedOver(Long id);

    void markExitInterviewCompleted(Long id);

    OffboardingChecklistResponse delete(Long id);
}

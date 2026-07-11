package com.businessos.modules.servicedesk.servicecategory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    List<ServiceCategory> findByActiveTrueOrderBySortOrderAsc();

    List<ServiceCategory> findAllByOrderBySortOrderAsc();

    Optional<ServiceCategory> findByName(String name);

    boolean existsByName(String name);
}

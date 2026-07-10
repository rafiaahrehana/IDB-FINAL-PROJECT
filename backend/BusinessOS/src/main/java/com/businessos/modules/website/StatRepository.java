package com.businessos.modules.website;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StatRepository extends JpaRepository<Stat, Long> {
    List<Stat> findByCompanyId(Long companyId);
}

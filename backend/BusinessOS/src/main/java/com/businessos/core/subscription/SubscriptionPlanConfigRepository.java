package com.businessos.core.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanConfigRepository extends JpaRepository<SubscriptionPlanConfig, Long> {
    List<SubscriptionPlanConfig> findAllByOrderByPlanAsc();
    List<SubscriptionPlanConfig> findAllByActiveTrueOrderByPlanAsc();
    Optional<SubscriptionPlanConfig> findByPlan(SubscriptionPlan plan);
}

package com.businessos.core.subscription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionPlanSeeder implements CommandLineRunner {

    private final SubscriptionPlanConfigRepository planConfigRepository;

    @Override
    public void run(String... args) {
        if (planConfigRepository.count() > 0) return;

        planConfigRepository.save(SubscriptionPlanConfig.builder()
                .plan(SubscriptionPlan.FREE)
                .displayName("Free Trial")
                .description("14-day free trial — limited access to platform features")
                .price(BigDecimal.ZERO)
                .currency("BDT")
                .durationDays(14)
                .featured(false)
                .active(true)
                .maxEmployees(5)
                .maxClients(10)
                .maxProjects(2)
                .maxStorageMb(500)
                .aiEnabled(false)
                .websiteBuilderEnabled(false)
                .customDomainEnabled(false)
                .prioritySupport(false)
                .apiAccess(false)
                .build());

        planConfigRepository.save(SubscriptionPlanConfig.builder()
                .plan(SubscriptionPlan.STARTER)
                .displayName("Starter")
                .description("For small teams getting started with basic features")
                .price(new BigDecimal("5000"))
                .currency("BDT")
                .durationDays(30)
                .featured(false)
                .active(true)
                .maxEmployees(15)
                .maxClients(50)
                .maxProjects(10)
                .maxStorageMb(2048)
                .aiEnabled(false)
                .websiteBuilderEnabled(true)
                .customDomainEnabled(false)
                .prioritySupport(false)
                .apiAccess(false)
                .build());

        planConfigRepository.save(SubscriptionPlanConfig.builder()
                .plan(SubscriptionPlan.PRO)
                .displayName("Professional")
                .description("For growing businesses with advanced features")
                .price(new BigDecimal("10000"))
                .currency("BDT")
                .durationDays(30)
                .featured(true)
                .active(true)
                .maxEmployees(50)
                .maxClients(200)
                .maxProjects(50)
                .maxStorageMb(10240)
                .aiEnabled(true)
                .websiteBuilderEnabled(true)
                .customDomainEnabled(true)
                .prioritySupport(true)
                .apiAccess(false)
                .build());

        planConfigRepository.save(SubscriptionPlanConfig.builder()
                .plan(SubscriptionPlan.ENTERPRISE)
                .displayName("Enterprise")
                .description("For large organizations with unlimited access")
                .price(new BigDecimal("20000"))
                .currency("BDT")
                .durationDays(30)
                .featured(false)
                .active(true)
                .maxEmployees(0)
                .maxClients(0)
                .maxProjects(0)
                .maxStorageMb(0)
                .aiEnabled(true)
                .websiteBuilderEnabled(true)
                .customDomainEnabled(true)
                .prioritySupport(true)
                .apiAccess(true)
                .build());

        log.info("Seeded 4 subscription plan configs");
    }
}

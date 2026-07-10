package com.businessos.core.subscription;

import com.businessos.enums.CompanyStatus;
import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.shared.payment.sslcommerz.SslCommerzInitRequest;
import com.businessos.shared.payment.sslcommerz.SslCommerzInitResponse;
import com.businessos.shared.payment.sslcommerz.SslCommerzService;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CompanyRepository companyRepository;
    private final SecurityUtil securityUtil;
    private final SslCommerzService sslCommerzService;
    private final SubscriptionPlanConfigRepository planConfigRepository;

    private static final Map<String, PlanInfo> PLANS = new LinkedHashMap<>();

    static {
        PLANS.put("FREE",       new PlanInfo("FREE",       "14-day free trial — limited access",       0,     14, false));
        PLANS.put("STARTER",    new PlanInfo("STARTER",    "For small teams getting started",       5000,   30, false));
        PLANS.put("PRO",        new PlanInfo("PRO",        "For growing businesses",               10000,   30, true));
        PLANS.put("ENTERPRISE", new PlanInfo("ENTERPRISE", "For large organizations",              20000,   30, false));
    }

    @Override
    public List<PlanInfo> getPlans() {
        List<SubscriptionPlanConfig> dbPlans = planConfigRepository.findAllByActiveTrueOrderByPlanAsc();
        if (!dbPlans.isEmpty()) {
            return dbPlans.stream()
                    .map(c -> new PlanInfo(c.getPlan().name(), c.getDescription(),
                            c.getPrice().intValue(), c.getDurationDays(), c.isFeatured()))
                    .collect(java.util.stream.Collectors.toList());
        }
        return new ArrayList<>(PLANS.values());
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getCurrentSubscription() {
        Long companyId = requireCompanyId();
        Subscription sub = subscriptionRepository
                .findTopByCompanyIdAndStatusOrderByEndDateDesc(companyId, Subscription.SubscriptionStatus.ACTIVE)
                .orElse(null);
        if (sub == null) {
            return null;
        }
        return toResponse(sub);
    }

    @Override
    @Transactional
    public SslCommerzInitResponse checkout(String planName, String cusName, String cusEmail) {
        Long companyId = requireCompanyId();
        PlanInfo plan = PLANS.get(planName.toUpperCase());
        if (plan == null || plan.price() == 0) {
            throw new BadRequestException("Invalid plan for payment");
        }

        // Check if already on this plan and active
        Subscription current = subscriptionRepository
                .findTopByCompanyIdAndStatusOrderByEndDateDesc(companyId, Subscription.SubscriptionStatus.ACTIVE)
                .orElse(null);
        if (current != null && current.getPlan().name().equals(planName.toUpperCase())
                && current.getEndDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("You already have an active " + planName + " subscription until " + current.getEndDate());
        }

        SslCommerzInitRequest sslReq = new SslCommerzInitRequest();
        sslReq.setAmount(BigDecimal.valueOf(plan.price()));
        sslReq.setCurrency("BDT");
        sslReq.setCusName(cusName);
        sslReq.setCusEmail(cusEmail);
        sslReq.setCusPhone("");

        SslCommerzInitResponse sslResp = sslCommerzService.initPaymentForSubscription(
                sslReq, companyId, planName.toUpperCase(), plan.durationDays());

        return sslResp;
    }

    @Override
    @Transactional
    public void activateAfterPayment(String tranId) {
        Subscription pending = subscriptionRepository.findByTranId(tranId).orElse(null);
        if (pending == null) return;

        Company company = companyRepository.findById(pending.getCompanyId()).orElse(null);
        if (company == null) return;

        // Extend from today or from existing end date if still active
        LocalDate start = LocalDate.now();
        LocalDate existingEnd = company.getSubscriptionEnd();
        if (existingEnd != null && existingEnd.isAfter(start)) {
            start = existingEnd;
        }
        LocalDate end = start.plusDays(pending.getDurationMonths());

        company.setSubscriptionPlan(SubscriptionPlan.valueOf(pending.getPlan().name()));
        company.setSubscriptionStart(start.minusDays(pending.getDurationMonths()));
        company.setSubscriptionEnd(end);
        company.setStatus(CompanyStatus.ACTIVE);
        company.setActive(true);
        companyRepository.save(company);

        pending.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        pending.setStartDate(start.minusDays(pending.getDurationMonths()));
        pending.setEndDate(end);
        subscriptionRepository.save(pending);

        log.info("Subscription activated for company {}: {} until {}", company.getId(), pending.getPlan(), end);
    }

    @Override
    @Transactional
    public void cancelSubscription(Long id) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        Long companyId = requireCompanyId();
        if (!sub.getCompanyId().equals(companyId)) {
            throw new BadRequestException("Not your subscription");
        }
        sub.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(sub);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?")
    public void expireOverdueSubscriptions() {
        List<Subscription> expired = subscriptionRepository.findExpiredSubscriptions(LocalDate.now());
        for (Subscription sub : expired) {
            sub.setStatus(Subscription.SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(sub);

            Company company = companyRepository.findById(sub.getCompanyId()).orElse(null);
            if (company != null && company.getSubscriptionEnd() != null
                    && company.getSubscriptionEnd().isBefore(LocalDate.now())) {
                company.setSubscriptionPlan(SubscriptionPlan.FREE);
                company.setStatus(CompanyStatus.SUSPENDED);
                companyRepository.save(company);
            }
        }
        log.info("Expired {} overdue subscriptions — companies suspended", expired.size());
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendRenewalReminders() {
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(7);
        List<Subscription> needingReminder = subscriptionRepository.findActiveNeedingRenewalReminder(today, cutoff);
        for (Subscription sub : needingReminder) {
            sub.setReminderSent(true);
            subscriptionRepository.save(sub);
            log.info("Renewal reminder sent for company {} plan {}", sub.getCompanyId(), sub.getPlan());
        }
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendExpiryReminders() {
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(3);
        List<Subscription> needingExpiry = subscriptionRepository.findActiveNeedingExpiryReminder(today, cutoff);
        for (Subscription sub : needingExpiry) {
            sub.setExpiryReminderSent(true);
            subscriptionRepository.save(sub);
            log.info("Expiry reminder sent for company {} plan {}", sub.getCompanyId(), sub.getPlan());
        }
    }

    private SubscriptionResponse toResponse(Subscription s) {
        SubscriptionResponse r = new SubscriptionResponse();
        r.setId(s.getId());
        r.setPlan(s.getPlan().name());
        r.setAmount(s.getAmount());
        r.setCurrency(s.getCurrency());
        r.setStartDate(s.getStartDate());
        r.setEndDate(s.getEndDate());
        r.setStatus(s.getStatus().name());
        r.setPaymentMethod(s.getPaymentMethod());
        r.setDurationMonths(s.getDurationMonths());
        r.setAutoRenew(s.isAutoRenew());
        return r;
    }

    private Long requireCompanyId() {
        Long id = securityUtil.getCurrentCompanyId();
        if (id == null) throw new BadRequestException("No company context");
        return id;
    }
}

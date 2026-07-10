package com.businessos.core.scheduler;

import com.businessos.modules.company.Company;
import com.businessos.enums.CompanyStatus;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.shared.email.EmailService;
import com.businessos.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final CompanyRepository companyRepository;
    private final EmailService emailService;

    @Value("${app.trial-reminder-days:3}")
    private int trialReminderDays;

    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void suspendExpiredCompanies() {
        List<Company> expired = companyRepository.findExpiredSubscriptions(
            LocalDate.now(),
            List.of(CompanyStatus.TRIAL, CompanyStatus.ACTIVE)
        );
        if (expired.isEmpty()) return;

        for (Company company : expired) {
            company.setStatus(CompanyStatus.SUSPENDED);
            if (company.getOwner() != null) {
                try {
                    emailService.sendSubscriptionSuspendedEmail(
                        company.getOwner().getEmail(),
                        company.getOwner().getFirstName(),
                        company.getCompanyName());
                } catch (Exception e) {
                    throw new BadRequestException("Suspension email failed for company: " +
                            company.getId() + " - " + e.getMessage());
                }
            }
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendSubscriptionExpiryReminders() {
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(trialReminderDays);

        List<Company> approachingTrials = companyRepository.findTrialExpiringBetween(today, cutoff,
                CompanyStatus.TRIAL);
        List<Company> approachingActive = companyRepository.findTrialExpiringBetween(today, cutoff,
                CompanyStatus.ACTIVE);

        processReminders(approachingTrials, today);
        processReminders(approachingActive, today);
    }

    private void processReminders(List<Company> approaching, LocalDate today) {
        if (approaching.isEmpty())
            return;

        for (Company company : approaching) {
            if (company.getOwner() == null)
                continue;
            try {
                long daysLeft = ChronoUnit.DAYS.between(today, company.getSubscriptionEnd());
                emailService.sendSubscriptionExpiryReminder(
                        company.getOwner().getEmail(),
                        company.getOwner().getFirstName(),
                        company.getCompanyName(),
                        (int) daysLeft);
                company.setTrialReminderSentAt(LocalDateTime.now());
            } catch (Exception e) {
                throw new BadRequestException("Expiry reminder failed for company: " +
                        company.getId() + " - " + e.getMessage());
            }
        }
    }
}

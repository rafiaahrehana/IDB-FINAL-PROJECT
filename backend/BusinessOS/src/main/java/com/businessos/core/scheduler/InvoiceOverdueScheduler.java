package com.businessos.core.scheduler;

import com.businessos.modules.finance.invoice.ClientInvoiceRepository;
import com.businessos.shared.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.businessos.enums.InvoiceStatus;
import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor

public class InvoiceOverdueScheduler {

    private final ClientInvoiceRepository invoiceRepository;
    private final NotificationRepository  notificationRepository;

    /**
     * Marks ISSUED and PARTIALLY_PAID invoices as OVERDUE when their dueDate has passed.
     * Runs daily at 01:30. Uses a single bulk UPDATE.
     */
    @Scheduled(cron = "0 30 1 * * *")
    @Transactional
    public void markOverdueInvoices() {
        int count = invoiceRepository.markOverdueInvoices(
            LocalDate.now(),
            InvoiceStatus.OVERDUE,
            List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID));
        if (count > 0) {
            
        }
    }

    /**
     * Cleans up read notifications older than 90 days.
     * Runs monthly on the 1st at 03:30.
     */
    @Scheduled(cron = "0 30 3 1 * *")
    @Transactional
    public void cleanOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
        int count = notificationRepository.deleteReadOlderThan(cutoff);
        if (count > 0) {
            
        }
    }
}

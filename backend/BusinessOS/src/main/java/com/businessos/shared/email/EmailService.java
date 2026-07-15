package com.businessos.shared.email;

public interface EmailService {

    void send(String to, String subject, String html);

    void sendVerificationEmail(String to, String name, String token);

    void sendPasswordResetEmail(String to, String name, String token);

    void sendWelcomeCompanyEmail(String to, String name, String companyName);

    void sendSubscriptionPurchasedEmail(String to, String name, String companyName);

    void sendSubscriptionExpiryReminder(String to, String name, String companyName, int daysLeft);

    void sendSubscriptionSuspendedEmail(String to, String name, String companyName);

    void sendLicenseExpiryReminder(String to, String name, String softwareName, java.time.LocalDate expiryDate, long daysLeft);

    void sendLicenseExpiredEmail(String to, String name, String softwareName, java.time.LocalDate expiryDate, int seatsUsed);

    void sendEmployeeWelcomeEmail(String to, String name, EmailBranding.Data branding);

    void sendOfferLetterEmail(String to, String name, EmailBranding.Data branding);

    void sendLeaveApprovalEmail(String to, String name, EmailBranding.Data branding);

    void sendSalaryRevisionEmail(String to, String name, EmailBranding.Data branding);

    void sendPayrollEmail(String to, String name, EmailBranding.Data branding);

    void sendInvoiceEmail(String to, String name, EmailBranding.Data branding);

    void sendTicketAssignedEmail(String to, String name, String ticketTitle, EmailBranding.Data branding);

    void sendClientWelcomeEmail(String to, String name, EmailBranding.Data branding);

    void sendTerminationEmail(String to, String name, EmailBranding.Data branding);

    void sendPerformanceReviewEmail(String to, String name, EmailBranding.Data branding);

    void sendPaymentReceiptEmail(String to, String name, String invoiceNumber, String amount, EmailBranding.Data branding);

    void sendExpenseStatusEmail(String to, String name, String expenseTitle, String status, EmailBranding.Data branding);

    void sendTicketCreatedEmail(String to, String name, String ticketTitle, EmailBranding.Data branding);

    void sendTicketResolvedEmail(String to, String name, String ticketTitle, EmailBranding.Data branding);

    void sendServiceRequestPaymentReminderEmail(String to, String name, String requestTitle, EmailBranding.Data branding);
}
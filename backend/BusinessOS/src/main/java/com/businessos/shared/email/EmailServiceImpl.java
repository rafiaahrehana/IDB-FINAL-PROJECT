package com.businessos.shared.email;

import com.businessos.shared.exception.BadRequestException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplate template;
    private final EmailBranding brandingHelper;
    private final EmailLogRepository emailLogRepository;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            EmailTemplate template,
            EmailBranding brandingHelper,
            EmailLogRepository emailLogRepository) {
        this.mailSender = mailSender;
        this.template = template;
        this.brandingHelper = brandingHelper;
        this.emailLogRepository = emailLogRepository;
    }

    @Value("${spring.mail.username}")
    private String from;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Async
    @Override
    public void send(String to, String subject, String html) {
        sendInternal(to, subject, html, "Custom");
    }

    private void sendInternal(String to, String subject, String html, String templateName) {
        sendInternal(to, subject, html, templateName, null);
    }

    private void sendInternal(String to, String subject, String html, String templateName, Long companyId) {
        com.businessos.modules.company.Company company = null;
        if (companyId != null) {
            company = new com.businessos.modules.company.Company();
            company.setId(companyId);
        }

        EmailLog emailLog = EmailLog.builder()
                .recipient(to)
                .subject(subject)
                .template(templateName)
                .company(company)
                .sentTime(LocalDateTime.now())
                .status("PENDING")
                .build();
        emailLog = emailLogRepository.save(emailLog);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);

            emailLog.setStatus("SUCCESS");
            emailLogRepository.save(emailLog);
            
        } catch (Exception e) {
            emailLog.setStatus("FAILED");
            emailLog.setFailureReason(e.getMessage());
            emailLogRepository.save(emailLog);
            throw new com.businessos.shared.exception.InternalServerException("Email delivery failed");
        }
    }

    @Async
    @Override
    public void sendVerificationEmail(String to, String name, String token) {
        String link = frontendUrl + "/verify-email?token=" + token;
        String html = template.build(brandingHelper.getPlatformBranding(), "", "Welcome " + name + ", please verify your email.", "Verify Email", link);
        sendInternal(to, "Verify your businessos account", html, "VerificationEmail");
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String to, String name, String token) {
        String link = frontendUrl + "/auth/reset-password?token=" + token;
        String html = template.build(brandingHelper.getPlatformBranding(), "", "Hi " + name + ", we received a request to reset your password.", "Reset Password", link);
        sendInternal(to, "Reset your businessos password", html, "PasswordResetEmail");
    }

    @Async
    @Override
    public void sendWelcomeCompanyEmail(String to, String name, String companyName) {
        String html = template.build(brandingHelper.getPlatformBranding(), "", "Welcome, " + name + "! " + companyName + " is now active on businessos.", "Go to Dashboard", frontendUrl + "/dashboard");
        sendInternal(to, "Welcome to businessos — " + companyName + " is live!", html, "WelcomeCompanyEmail");
    }

    @Async
    @Override
    public void sendSubscriptionPurchasedEmail(String to, String name, String companyName) {
        String html = template.build(brandingHelper.getPlatformBranding(), "", "Hi " + name + ", your subscription for " + companyName + " is active.", "View Billing", frontendUrl + "/settings/billing");
        sendInternal(to, "Subscription activated for " + companyName, html, "SubscriptionPurchased");
    }

    @Async
    @Override
    public void sendSubscriptionExpiryReminder(String to, String name, String companyName, int daysLeft) {
        String message = daysLeft == 0 ? "Your subscription for " + companyName + " expires today." : "Your subscription for " + companyName + " expires in " + daysLeft + " days.";
        String html = template.build(brandingHelper.getPlatformBranding(), "", message, "Upgrade Now", frontendUrl + "/settings/billing");
        sendInternal(to, "Your businessos subscription expires soon", html, "SubscriptionExpiryReminder");
    }

    @Async
    @Override
    public void sendSubscriptionSuspendedEmail(String to, String name, String companyName) {
        String html = template.build(brandingHelper.getPlatformBranding(), "", "Hi " + name + ", your account for " + companyName + " has been suspended.", "Reactivate Now", frontendUrl + "/settings/billing");
        sendInternal(to, "Your businessos account is suspended", html, "SubscriptionSuspended");
    }

    @Async
    @Override
    public void sendLicenseExpiryReminder(String to, String name, String softwareName, java.time.LocalDate expiryDate, long daysLeft) {
        String message = "Hi " + name + ", your " + softwareName + " license expires on " + expiryDate
                + " (" + daysLeft + " day" + (daysLeft == 1 ? "" : "s") + " left). Renew or cancel before it lapses.";
        String html = template.build(brandingHelper.getPlatformBranding(), "", message, "Review License", frontendUrl + "/itam/software");
        sendInternal(to, softwareName + " license expires soon", html, "LicenseExpiryReminder");
    }

    @Async
    @Override
    public void sendLicenseExpiredEmail(String to, String name, String softwareName, java.time.LocalDate expiryDate, int seatsUsed) {
        String message = "Hi " + name + ", your " + softwareName + " license expired on " + expiryDate
                + ". " + seatsUsed + " assigned seat(s) may lose access until it's renewed.";
        String html = template.build(brandingHelper.getPlatformBranding(), "", message, "Renew Now", frontendUrl + "/itam/software");
        sendInternal(to, softwareName + " license has expired", html, "LicenseExpired");
    }

    @Async
    @Override
    public void sendEmployeeWelcomeEmail(String to, String name, EmailBranding.Data branding) {
        String html = template.build(branding, "", "Welcome to " + branding.getCompanyName() + ", " + name + "!", "Login to Portal", frontendUrl + "/login");
        sendInternal(to, "Welcome to " + branding.getCompanyName(), html, "EmployeeWelcome");
    }

    @Async
    @Override
    public void sendOfferLetterEmail(String to, String name, EmailBranding.Data branding) {
        String html = template.build(branding, "", "Hi " + name + ", we are thrilled to offer you a position at " + branding.getCompanyName() + ".", "View Offer", frontendUrl + "/offer");
        sendInternal(to, "Offer Letter from " + branding.getCompanyName(), html, "OfferLetter");
    }

    @Async
    @Override
    public void sendLeaveApprovalEmail(String to, String name, EmailBranding.Data branding) {
        String html = template.build(branding, "", "Hi " + name + ", your leave request has been approved.", "View Leaves", frontendUrl + "/leaves");
        sendInternal(to, "Leave Approved", html, "LeaveApproval", branding.getCompanyId());
    }

    @Async
    @Override
    public void sendSalaryRevisionEmail(String to, String name, EmailBranding.Data branding) {
        String subject = "Salary Revision Notification";
        String html = EmailTemplate.buildSalaryRevisionTemplate(name, branding);
        sendInternal(to, subject, html, "salary-revision", branding.getCompanyId());
    }

    @Async
    @Override
    public void sendPayrollEmail(String to, String name, EmailBranding.Data branding) {
        String html = template.build(branding, "", "Hi " + name + ", your payslip is ready.", "View Payslip", frontendUrl + "/payroll");
        sendInternal(to, "Your Payslip is Ready", html, "Payroll");
    }

    @Async
    @Override
    public void sendInvoiceEmail(String to, String name, EmailBranding.Data branding) {
        String html = template.build(branding, "", "Hi " + name + ", a new invoice has been generated.", "View Invoice", frontendUrl + "/invoices");
        sendInternal(to, "New Invoice from " + branding.getCompanyName(), html, "Invoice");
    }

    @Async
    @Override
    public void sendTicketAssignedEmail(String to, String name, String ticketTitle, EmailBranding.Data branding) {
        String subject = "Ticket Assigned: " + ticketTitle;
        String html = EmailTemplate.buildTicketAssignedTemplate(name, ticketTitle, branding);
        sendInternal(to, subject, html, "ticket-assigned", branding.getCompanyId());
    }

    @Async
    @Override
    public void sendClientWelcomeEmail(String to, String name, EmailBranding.Data branding) {
        String html = template.build(branding, "", "Hi " + name + ", welcome to the " + branding.getCompanyName() + " client portal.", "Access Portal", frontendUrl + "/client-login");
        sendInternal(to, "Welcome to " + branding.getCompanyName(), html, "ClientWelcome");
    }

    @Async
    @Override
    public void sendTerminationEmail(String to, String name, EmailBranding.Data branding) {
        String subject = "Offboarding Notification";
        String html = EmailTemplate.buildTerminationTemplate(name, branding);
        sendInternal(to, subject, html, "termination", branding.getCompanyId());
    }

    @Async
    @Override
    public void sendPerformanceReviewEmail(String to, String name, EmailBranding.Data branding) {
        String subject = "Performance Review Scheduled";
        String html = EmailTemplate.buildPerformanceReviewTemplate(name, branding);
        sendInternal(to, subject, html, "performance-review", branding.getCompanyId());
    }

    @Async
    @Override
    public void sendPaymentReceiptEmail(String to, String name, String invoiceNumber, String amount, EmailBranding.Data branding) {
        String subject = "Payment Receipt: " + invoiceNumber;
        String html = EmailTemplate.buildPaymentReceiptTemplate(name, invoiceNumber, amount, branding);
        sendInternal(to, subject, html, "payment-receipt", branding.getCompanyId());
    }

    @Async
    @Override
    public void sendExpenseStatusEmail(String to, String name, String expenseTitle, String status, EmailBranding.Data branding) {
        String subject = "Expense Request " + status + ": " + expenseTitle;
        String html = EmailTemplate.buildExpenseStatusTemplate(name, expenseTitle, status, branding);
        sendInternal(to, subject, html, "expense-status", branding.getCompanyId());
    }

    @Async
    @Override
    public void sendTicketCreatedEmail(String to, String name, String ticketTitle, EmailBranding.Data branding) {
        String subject = "Support Ticket Received: " + ticketTitle;
        String html = EmailTemplate.buildTicketCreatedTemplate(name, ticketTitle, branding);
        sendInternal(to, subject, html, "ticket-created", branding.getCompanyId());
    }

    @Async
    @Override
    public void sendTicketResolvedEmail(String to, String name, String ticketTitle, EmailBranding.Data branding) {
        String subject = "Support Ticket Resolved: " + ticketTitle;
        String html = EmailTemplate.buildTicketResolvedTemplate(name, ticketTitle, branding);
        sendInternal(to, subject, html, "ticket-resolved", branding.getCompanyId());
    }

    @Async
    @Override
    public void sendServiceRequestPaymentReminderEmail(String to, String name, String requestTitle, EmailBranding.Data branding) {
        String subject = "Action Required: Payment Reminder for Service Request - " + requestTitle;
        String html = template.build(branding, "", "Hi " + name + ", this is a reminder to complete the payment for your service request '" + requestTitle + "'. Your request will be automatically cancelled if not paid within 24 hours.", "Pay Now", frontendUrl + "/invoices");
        sendInternal(to, subject, html, "service-request-reminder", branding.getCompanyId());
    }
}

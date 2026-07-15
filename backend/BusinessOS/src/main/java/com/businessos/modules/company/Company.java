package com.businessos.modules.company;

import com.businessos.core.base.BaseEntity;
import com.businessos.auth.user.User;
import com.businessos.enums.CompanyStatus;
import com.businessos.enums.SubscriptionPlan;
import com.businessos.shared.address.Address;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Company extends BaseEntity {

    @Column(nullable = false)
    private String companyName;

    @Column(unique = true)
    private String companyEmail;

    @Column(unique = true)
    private String companyPhone;

    private String website;

    @Column(nullable = false, unique = true)
    private String subdomain;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_detail_id")
    private Address locationDetail;

    @Column(columnDefinition = "TEXT")
    private String portalAbout;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private CompanyStatus status = CompanyStatus.PENDING_VERIFICATION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE;

    @Builder.Default
    private boolean active = false;
    @Builder.Default
    private boolean emailVerified = false;
    @Builder.Default
    private boolean trialReminderSent = false;

    private LocalDate subscriptionStart;
    private LocalDate subscriptionEnd;

    private LocalDateTime trialReminderSentAt;

    @Builder.Default
    private boolean isPlatformTenant = false;

    public boolean isTrialExpired() {
        if (this.isPlatformTenant) return false;
        return subscriptionEnd != null && subscriptionEnd.isBefore(LocalDate.now());
    }
}

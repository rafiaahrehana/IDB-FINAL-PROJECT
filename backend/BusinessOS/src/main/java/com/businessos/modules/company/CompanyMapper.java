package com.businessos.modules.company;

import com.businessos.auth.user.User;

public class CompanyMapper {
    public static CompanyResponse toResponse(Company c) {
        User owner = c.getOwner();
        CompanyResponse r = new CompanyResponse();
        r.setId(c.getId());
        r.setCompanyName(c.getCompanyName());
        r.setSubdomain(c.getSubdomain());
        r.setCompanyEmail(c.getCompanyEmail());
        r.setCompanyPhone(c.getCompanyPhone());
        r.setWebsite(c.getWebsite());
        r.setLocation(c.getLocation());
        r.setLogo(c.getLogo());
        r.setPrimaryColor(c.getPrimaryColor());
        r.setSecondaryColor(c.getSecondaryColor());
        r.setTagline(c.getTagline());
        r.setPortalAbout(c.getPortalAbout());
        r.setStatus(c.getStatus());
        r.setSubscriptionPlan(c.getSubscriptionPlan());
        r.setSubscriptionStart(c.getSubscriptionStart());
        r.setSubscriptionEnd(c.getSubscriptionEnd());
        r.setTrialExpired(c.isTrialExpired());
        r.setOwnerId(owner != null ? owner.getId() : null);
        r.setOwnerName(owner != null ? owner.getFullName() : null);
        r.setOwnerEmail(owner != null ? owner.getEmail() : null);
        r.setCreatedAt(c.getCreatedAt());
        return r;
    }

    public static CompanyPublicResponse toPublicResponse(Company c) {
        CompanyPublicResponse r = new CompanyPublicResponse();
        r.setId(c.getId());
        r.setCompanyName(c.getCompanyName());
        r.setSubdomain(c.getSubdomain());
        r.setLogo(c.getLogo());
        r.setPrimaryColor(c.getPrimaryColor());
        r.setSecondaryColor(c.getSecondaryColor());
        r.setTagline(c.getTagline());
        r.setPortalAbout(c.getPortalAbout());
        r.setWebsite(c.getWebsite());
        r.setLocation(c.getLocation());
        r.setCompanyPhone(c.getCompanyPhone());
        r.setCompanyEmail(c.getCompanyEmail());
        return r;
    }
}

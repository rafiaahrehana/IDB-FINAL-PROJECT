package com.businessos.shared.email;

import com.businessos.modules.company.Company;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailBranding {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public Data getPlatformBranding() {
        return Data.builder()
                .companyId(null)
                .companyName("businessos")
                .logoUrl(frontendUrl + "/images/logo.png")
                .primaryColor("#1e3a5f")
                .build();
    }

    public Data from(Company company) {
        String logoUrl = (company.getLogo() != null)
                ? frontendUrl + "/images/company/" + company.getLogo()
                : null;

        return Data.builder()
                .companyId(company.getId())
                .companyName(company.getCompanyName())
                .logoUrl(logoUrl)
                .primaryColor(company.getPrimaryColor())
                .build();
    }

    @Getter
    @Builder
    public static class Data {
        private Long companyId;
        private String companyName;
        private String logoUrl;
        private String primaryColor;
    }
}
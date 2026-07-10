package com.businessos.modules.servicedesk.servicecategory;

import com.businessos.core.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * FIXES:
 * 1. Added @Builder @AllArgsConstructor — controller uses ServiceCategory.builder()
 * 2. Added 'sortOrder' field — controller, mapper, response, and repository all reference it
 */
@Entity
@Table(name = "service_categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceCategory extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String iconUrl;

    @Builder.Default
    private boolean active = true;

    private String nameBn;
    private String descriptionBn;

    @Column(nullable = false)
    @Builder.Default
    private int sortOrder = 0;
}

package com.businessos.modules.website;

import com.businessos.core.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDateTime;

@Entity
@Table(name = "website_blog_posts")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "companyId", type = Long.class))
@Filter(name = "tenantFilter", condition = "company_id = :companyId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogPost extends BaseEntity {
    @Column(name = "company_id", nullable = false)
    private Long companyId;
    @Column(unique = true)
    private String slug;
    private String title;
    @Column(length = 600)
    private String excerpt;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String body;
    private String coverImageUrl;
    private String author;
    private LocalDateTime publishedAt;
    private String category;
    private int readMinutes;
}

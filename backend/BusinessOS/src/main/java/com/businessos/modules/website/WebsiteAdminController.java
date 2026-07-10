package com.businessos.modules.website;

import com.businessos.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/website/admin")
@PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
public class WebsiteAdminController {

    private final SecurityUtil securityUtil;
    private final WebsiteSettingsRepository settingsRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final BlogRepository blogRepository;
    private final FaqRepository faqRepository;
    private final CmsPageRepository cmsPageRepository;
    private final NavItemRepository navItemRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final PortalProjectRepository projectRepository;
    private final PricingPlanRepository pricingPlanRepository;
    private final StatRepository statRepository;
    private final TestimonialRepository testimonialRepository;

    private Long cid() {
        Long id = securityUtil.getCurrentCompanyId();
        if (id == null) throw new RuntimeException("No company context");
        return id;
    }

    // ---- Settings ----
    @PutMapping("/settings")
    public WebsiteSettings saveSettings(@RequestBody WebsiteSettings s) {
        WebsiteSettings existing = settingsRepository.findByCompanyId(cid()).orElse(null);
        if (existing != null) {
            s.setId(existing.getId());
            s.setCreatedAt(existing.getCreatedAt());
        }
        s.setCompanyId(cid());
        return settingsRepository.save(s);
    }

    // ---- Services ----
    @PostMapping("/services")
    public Service createService(@RequestBody Service s) {
        s.setCompanyId(cid());
        return serviceRepository.save(s);
    }

    @PutMapping("/services/{id}")
    public Service updateService(@PathVariable Long id, @RequestBody Service s) {
        s.setId(id);
        s.setCompanyId(cid());
        return serviceRepository.save(s);
    }

    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Service categories ----
    @PostMapping("/categories")
    public ServiceCategory createCategory(@RequestBody ServiceCategory c) {
        c.setCompanyId(cid());
        return serviceCategoryRepository.save(c);
    }

    // ---- Blog ----
    @PostMapping("/blog")
    public BlogPost createBlog(@RequestBody BlogPost b) {
        b.setCompanyId(cid());
        if (b.getPublishedAt() == null) b.setPublishedAt(LocalDateTime.now());
        return blogRepository.save(b);
    }

    @PutMapping("/blog/{id}")
    public BlogPost updateBlog(@PathVariable Long id, @RequestBody BlogPost b) {
        b.setId(id);
        b.setCompanyId(cid());
        return blogRepository.save(b);
    }

    @DeleteMapping("/blog/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        blogRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- FAQs ----
    @PostMapping("/faqs")
    public Faq createFaq(@RequestBody Faq f) {
        f.setCompanyId(cid());
        return faqRepository.save(f);
    }

    @DeleteMapping("/faqs/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        faqRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- CMS pages ----
    @PutMapping("/pages")
    public CmsPage savePage(@RequestBody CmsPage p) {
        p.setCompanyId(cid());
        p.setUpdatedAt(LocalDateTime.now());
        return cmsPageRepository.save(p);
    }

    @DeleteMapping("/pages/{id}")
    public ResponseEntity<Void> deletePage(@PathVariable Long id) {
        cmsPageRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Nav ----
    @PostMapping("/nav")
    public NavItem createNav(@RequestBody NavItem n) {
        n.setCompanyId(cid());
        return navItemRepository.save(n);
    }

    @PutMapping("/nav/{id}")
    public NavItem updateNav(@PathVariable Long id, @RequestBody NavItem n) {
        n.setId(id);
        n.setCompanyId(cid());
        return navItemRepository.save(n);
    }

    @DeleteMapping("/nav/{id}")
    public ResponseEntity<Void> deleteNav(@PathVariable Long id) {
        navItemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Team / Projects / Pricing / Stats / Testimonials ----
    @PostMapping("/team")
    public TeamMember createTeam(@RequestBody TeamMember m) {
        m.setCompanyId(cid());
        return teamMemberRepository.save(m);
    }

    @PostMapping("/projects")
    public PortalProject createProject(@RequestBody PortalProject p) {
        p.setCompanyId(cid());
        return projectRepository.save(p);
    }

    @PostMapping("/pricing")
    public PricingPlan createPricing(@RequestBody PricingPlan p) {
        p.setCompanyId(cid());
        return pricingPlanRepository.save(p);
    }

    @PostMapping("/stats")
    public Stat createStat(@RequestBody Stat s) {
        s.setCompanyId(cid());
        return statRepository.save(s);
    }

    @PostMapping("/testimonials")
    public Testimonial createTestimonial(@RequestBody Testimonial t) {
        t.setCompanyId(cid());
        return testimonialRepository.save(t);
    }
}

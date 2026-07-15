package com.businessos.modules.website;

import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ResourceNotFoundException;
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
    private final WebsiteService websiteService;
    private final WebsiteSettingsRepository settingsRepository;
    private final ServiceRepository serviceRepository;
    private final WebsiteContentRepository contentRepository;
    private final FaqRepository faqRepository;
    private final NavItemRepository navItemRepository;
    private final WebsitePersonRepository personRepository;
    private final PortalProjectRepository projectRepository;
    private final PricingPlanRepository pricingPlanRepository;

    private Long cid() {
        Long id = securityUtil.getCurrentCompanyId();
        if (id == null) throw new RuntimeException("No company context");
        return id;
    }

    // ---- Settings ----
    @GetMapping("/settings")
    public WebsiteSettings getSettings() {
        return websiteService.getSettings(cid());
    }

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
    @GetMapping("/services")
    public java.util.List<Service> getServices() {
        return serviceRepository.findByCompanyId(cid());
    }

    @PostMapping("/services")
    public Service createService(@RequestBody Service s) {
        s.setCompanyId(cid());
        return serviceRepository.save(s);
    }

    @PutMapping("/services/{id}")
    public Service updateService(@PathVariable Long id, @RequestBody Service s) {
        requireOwned(serviceRepository.findById(id).map(Service::getCompanyId), "Service");
        s.setId(id);
        s.setCompanyId(cid());
        return serviceRepository.save(s);
    }

    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        requireOwned(serviceRepository.findById(id).map(Service::getCompanyId), "Service");
        serviceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Blog ----
    @GetMapping("/blog")
    public java.util.List<WebsiteContent> getBlogPosts() {
        return contentRepository.findByCompanyIdAndType(cid(), ContentType.POST);
    }

    @PostMapping("/blog")
    public WebsiteContent createBlog(@RequestBody WebsiteContent b) {
        b.setCompanyId(cid());
        b.setType(ContentType.POST);
        if (b.getPublishedAt() == null) b.setPublishedAt(LocalDateTime.now());
        return contentRepository.save(b);
    }

    @PutMapping("/blog/{id}")
    public WebsiteContent updateBlog(@PathVariable Long id, @RequestBody WebsiteContent b) {
        requireOwned(contentRepository.findById(id).map(WebsiteContent::getCompanyId), "Blog post");
        b.setId(id);
        b.setCompanyId(cid());
        b.setType(ContentType.POST);
        return contentRepository.save(b);
    }

    @DeleteMapping("/blog/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        requireOwned(contentRepository.findById(id).map(WebsiteContent::getCompanyId), "Blog post");
        contentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- FAQs ----
    @GetMapping("/faqs")
    public java.util.List<Faq> getFaqs() {
        return faqRepository.findByCompanyId(cid());
    }

    @PostMapping("/faqs")
    public Faq createFaq(@RequestBody Faq f) {
        f.setCompanyId(cid());
        return faqRepository.save(f);
    }

    @PutMapping("/faqs/{id}")
    public Faq updateFaq(@PathVariable Long id, @RequestBody Faq f) {
        requireOwned(faqRepository.findById(id).map(Faq::getCompanyId), "FAQ");
        f.setId(id);
        f.setCompanyId(cid());
        return faqRepository.save(f);
    }

    @DeleteMapping("/faqs/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        requireOwned(faqRepository.findById(id).map(Faq::getCompanyId), "FAQ");
        faqRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- CMS pages ----
    @GetMapping("/pages")
    public java.util.List<WebsiteContent> getPages() {
        return contentRepository.findByCompanyIdAndType(cid(), ContentType.PAGE);
    }

    @PutMapping("/pages")
    public WebsiteContent savePage(@RequestBody WebsiteContent p) {
        if (p.getId() != null) {
            requireOwned(contentRepository.findById(p.getId()).map(WebsiteContent::getCompanyId), "Page");
        }
        p.setCompanyId(cid());
        p.setType(ContentType.PAGE);
        return contentRepository.save(p);
    }

    @DeleteMapping("/pages/{id}")
    public ResponseEntity<Void> deletePage(@PathVariable Long id) {
        requireOwned(contentRepository.findById(id).map(WebsiteContent::getCompanyId), "Page");
        contentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Nav ----
    @GetMapping("/nav")
    public java.util.List<NavItem> getNavItems() {
        return navItemRepository.findByCompanyIdOrderBySortOrderAsc(cid());
    }

    @PostMapping("/nav")
    public NavItem createNav(@RequestBody NavItem n) {
        n.setCompanyId(cid());
        return navItemRepository.save(n);
    }

    @PutMapping("/nav/{id}")
    public NavItem updateNav(@PathVariable Long id, @RequestBody NavItem n) {
        requireOwned(navItemRepository.findById(id).map(NavItem::getCompanyId), "Nav item");
        n.setId(id);
        n.setCompanyId(cid());
        return navItemRepository.save(n);
    }

    @DeleteMapping("/nav/{id}")
    public ResponseEntity<Void> deleteNav(@PathVariable Long id) {
        requireOwned(navItemRepository.findById(id).map(NavItem::getCompanyId), "Nav item");
        navItemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Team ----
    @GetMapping("/team")
    public java.util.List<WebsitePerson> getTeam() {
        return personRepository.findByCompanyIdAndType(cid(), PersonType.TEAM_MEMBER);
    }

    @PostMapping("/team")
    public WebsitePerson createTeam(@RequestBody WebsitePerson m) {
        m.setCompanyId(cid());
        m.setType(PersonType.TEAM_MEMBER);
        return personRepository.save(m);
    }

    @PutMapping("/team/{id}")
    public WebsitePerson updateTeam(@PathVariable Long id, @RequestBody WebsitePerson m) {
        requireOwned(personRepository.findById(id).map(WebsitePerson::getCompanyId), "Team member");
        m.setId(id);
        m.setCompanyId(cid());
        m.setType(PersonType.TEAM_MEMBER);
        return personRepository.save(m);
    }

    @DeleteMapping("/team/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        requireOwned(personRepository.findById(id).map(WebsitePerson::getCompanyId), "Team member");
        personRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Projects ----
    @GetMapping("/projects")
    public java.util.List<PortalProject> getProjects() {
        return projectRepository.findByCompanyId(cid());
    }

    @PostMapping("/projects")
    public PortalProject createProject(@RequestBody PortalProject p) {
        p.setCompanyId(cid());
        return projectRepository.save(p);
    }

    @PutMapping("/projects/{id}")
    public PortalProject updateProject(@PathVariable Long id, @RequestBody PortalProject p) {
        requireOwned(projectRepository.findById(id).map(PortalProject::getCompanyId), "Project");
        p.setId(id);
        p.setCompanyId(cid());
        return projectRepository.save(p);
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        requireOwned(projectRepository.findById(id).map(PortalProject::getCompanyId), "Project");
        projectRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Pricing ----
    @GetMapping("/pricing")
    public java.util.List<PricingPlan> getPricingPlans() {
        return pricingPlanRepository.findByCompanyId(cid());
    }

    @PostMapping("/pricing")
    public PricingPlan createPricing(@RequestBody PricingPlan p) {
        p.setCompanyId(cid());
        return pricingPlanRepository.save(p);
    }

    @PutMapping("/pricing/{id}")
    public PricingPlan updatePricing(@PathVariable Long id, @RequestBody PricingPlan p) {
        requireOwned(pricingPlanRepository.findById(id).map(PricingPlan::getCompanyId), "Pricing plan");
        p.setId(id);
        p.setCompanyId(cid());
        return pricingPlanRepository.save(p);
    }

    @DeleteMapping("/pricing/{id}")
    public ResponseEntity<Void> deletePricing(@PathVariable Long id) {
        requireOwned(pricingPlanRepository.findById(id).map(PricingPlan::getCompanyId), "Pricing plan");
        pricingPlanRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Stats ----
    @PostMapping("/stats")
    public Stat createStat(@RequestBody Stat s) {
        WebsiteSettings settings = websiteService.getSettings(cid());
        settings.getStats().add(s);
        settingsRepository.save(settings);
        return s;
    }

    // ---- Testimonials ----
    @GetMapping("/testimonials")
    public java.util.List<WebsitePerson> getTestimonials() {
        return personRepository.findByCompanyIdAndType(cid(), PersonType.TESTIMONIAL);
    }

    @PostMapping("/testimonials")
    public WebsitePerson createTestimonial(@RequestBody WebsitePerson t) {
        t.setCompanyId(cid());
        t.setType(PersonType.TESTIMONIAL);
        return personRepository.save(t);
    }

    @PutMapping("/testimonials/{id}")
    public WebsitePerson updateTestimonial(@PathVariable Long id, @RequestBody WebsitePerson t) {
        requireOwned(personRepository.findById(id).map(WebsitePerson::getCompanyId), "Testimonial");
        t.setId(id);
        t.setCompanyId(cid());
        t.setType(PersonType.TESTIMONIAL);
        return personRepository.save(t);
    }

    @DeleteMapping("/testimonials/{id}")
    public ResponseEntity<Void> deleteTestimonial(@PathVariable Long id) {
        requireOwned(personRepository.findById(id).map(WebsitePerson::getCompanyId), "Testimonial");
        personRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Verifies the record exists and belongs to the current tenant before
    // an update or delete is allowed.
    private void requireOwned(java.util.Optional<Long> ownerCompanyId, String what) {
        Long owner = ownerCompanyId
                .orElseThrow(() -> new ResourceNotFoundException(what + " not found"));
        if (!owner.equals(cid())) {
            throw new ResourceNotFoundException(what + " not found");
        }
    }
}

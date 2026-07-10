import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SiteService } from '../../services/site.service';
import { SiteSettings, Stat, Service, Testimonial, BlogPost, PricingPlan, Faq } from '../../models/site.model';
import { DEFAULT_SITE } from '../../services/default-site.config';
import { SiteHeroComponent } from '../../components/hero/hero.component';
import { StatCardComponent } from '../../components/stat-card/stat-card.component';
import { ServiceCardComponent } from '../../components/service-card/service-card.component';
import { TestimonialCardComponent } from '../../components/testimonial-card/testimonial-card.component';
import { BlogCardComponent } from '../../components/blog-card/blog-card.component';
import { FaqItemComponent } from '../../components/faq-item/faq-item.component';
import { PricingCardComponent } from '../../components/pricing-card/pricing-card.component';

@Component({
  selector: 'app-site-home',
  standalone: true,
  imports: [RouterLink, SiteHeroComponent, StatCardComponent, ServiceCardComponent, TestimonialCardComponent, BlogCardComponent, FaqItemComponent, PricingCardComponent],
  template: `
    <app-site-hero [settings]="settings"></app-site-hero>

    @if (stats.length) {
      <section class="py-5">
        <div class="container">
          <div class="row g-4">
            @for (s of stats; track s.id) {
              <div class="col-6 col-md-3"><app-stat-card [stat]="s"></app-stat-card></div>
            }
          </div>
        </div>
      </section>
    }

    <section class="py-5" style="background: #f8fafc">
      <div class="container">
        <div class="row align-items-center g-5">
          <div class="col-lg-6">
            <h2 class="fw-bold mb-3">About <span style="color: var(--site-primary)">Us</span></h2>
            <p class="text-muted mb-4">{{ settings?.aboutText }}</p>
            <div class="row g-3">
              <div class="col-6">
                <div class="p-3 rounded" style="background: rgba(var(--site-primary-rgb), 0.05)">
                  <h6 class="fw-bold mb-1" style="color: var(--site-primary)">Our Mission</h6>
                  <small class="text-muted">{{ settings?.mission }}</small>
                </div>
              </div>
              <div class="col-6">
                <div class="p-3 rounded" style="background: rgba(var(--site-primary-rgb), 0.05)">
                  <h6 class="fw-bold mb-1" style="color: var(--site-primary)">Our Vision</h6>
                  <small class="text-muted">{{ settings?.vision }}</small>
                </div>
              </div>
            </div>
          </div>
          <div class="col-lg-6 text-center">
            @if (settings?.heroImageUrl) {
              <img [src]="settings.heroImageUrl" class="img-fluid rounded shadow" alt="About" style="max-height: 360px">
            } @else {
              <div class="about-placeholder rounded"><i class="bi bi-building"></i></div>
            }
          </div>
        </div>
      </div>
    </section>

    @if (services.length) {
      <section class="py-5">
        <div class="container">
          <div class="text-center mb-5">
            <h2 class="fw-bold">Our <span style="color: var(--site-primary)">Services</span></h2>
            <p class="text-muted">Comprehensive solutions tailored to your business needs.</p>
          </div>
          <div class="row g-4">
            @for (s of services.slice(0, 4); track s.id) {
              <div class="col-md-6 col-lg-3"><app-service-card [service]="s"></app-service-card></div>
            }
          </div>
          <div class="text-center mt-4">
            <a routerLink="/services" class="btn btn-outline-dark px-4" style="border-radius: var(--site-btn-radius)">View All Services</a>
          </div>
        </div>
      </section>
    }

    @if (projects.length) {
      <section class="py-5" style="background: #f8fafc">
        <div class="container">
          <div class="text-center mb-5">
            <h2 class="fw-bold">Our <span style="color: var(--site-primary)">Work</span></h2>
            <p class="text-muted">Recent projects that showcase our expertise.</p>
          </div>
          <div class="row g-4">
            @for (p of projects.slice(0, 3); track p.id) {
              <div class="col-md-4">
                <div class="card h-100 border-0 shadow-sm overflow-hidden" style="border-radius: var(--site-radius)">
                  @if (p.coverImageUrl) {
                    <img [src]="p.coverImageUrl" class="card-img-top" [alt]="p.title" style="height: 200px; object-fit: cover">
                  } @else {
                    <div class="project-placeholder"><i class="bi bi-folder2-open"></i></div>
                  }
                  <div class="card-body p-4">
                    <span class="badge mb-2" style="background: var(--site-primary); color: #fff">{{ p.category }}</span>
                    <h5 class="fw-bold">{{ p.title }}</h5>
                    <p class="text-muted small">{{ p.summary }}</p>
                  </div>
                </div>
              </div>
            }
          </div>
          <div class="text-center mt-4">
            <a routerLink="/portfolio" class="btn btn-outline-dark px-4" style="border-radius: var(--site-btn-radius)">View Portfolio</a>
          </div>
        </div>
      </section>
    }

    @if (pricing.length) {
      <section class="py-5">
        <div class="container">
          <div class="text-center mb-5">
            <h2 class="fw-bold">Pricing <span style="color: var(--site-primary)">Plans</span></h2>
            <p class="text-muted">Simple, transparent pricing for every business.</p>
          </div>
          <div class="row g-4 justify-content-center">
            @for (p of pricing.slice(0, 3); track p.id) {
              <div class="col-md-6 col-lg-4"><app-pricing-card [plan]="p"></app-pricing-card></div>
            }
          </div>
        </div>
      </section>
    }

    @if (testimonials.length) {
      <section class="py-5" style="background: #f8fafc">
        <div class="container">
          <div class="text-center mb-5">
            <h2 class="fw-bold">What Clients <span style="color: var(--site-primary)">Say</span></h2>
          </div>
          <div class="row g-4">
            @for (t of testimonials; track t.id) {
              <div class="col-md-6 col-lg-4"><app-testimonial-card [t]="t"></app-testimonial-card></div>
            }
          </div>
        </div>
      </section>
    }

    @if (blogs.length) {
      <section class="py-5">
        <div class="container">
          <div class="text-center mb-5">
            <h2 class="fw-bold">Latest <span style="color: var(--site-primary)">Blog</span></h2>
            <p class="text-muted">Insights, tips and industry news.</p>
          </div>
          <div class="row g-4">
            @for (b of blogs.slice(0, 3); track b.id) {
              <div class="col-md-6 col-lg-4"><app-blog-card [post]="b"></app-blog-card></div>
            }
          </div>
          <div class="text-center mt-4">
            <a routerLink="/blog" class="btn btn-outline-dark px-4" style="border-radius: var(--site-btn-radius)">View All Posts</a>
          </div>
        </div>
      </section>
    }

    @if (faqs.length) {
      <section class="py-5" style="background: #f8fafc">
        <div class="container">
          <div class="row justify-content-center">
            <div class="col-lg-8">
              <div class="text-center mb-5">
                <h2 class="fw-bold">Frequently Asked <span style="color: var(--site-primary)">Questions</span></h2>
              </div>
              <div class="accordion" id="faqAccordion">
                @for (f of faqs.slice(0, 5); track f.id) {
                  <app-faq-item [faq]="f"></app-faq-item>
                }
              </div>
            </div>
          </div>
        </div>
      </section>
    }

    <section class="py-5">
      <div class="container">
        <div class="cta-box text-center text-white p-5 rounded" style="background: var(--site-gradient)">
          <h2 class="fw-bold mb-3">Ready to Get Started?</h2>
          <p class="mb-4 opacity-75">Let us help you take your business to the next level.</p>
          <a routerLink="/request-service" class="btn btn-light btn-lg px-5" style="border-radius: var(--site-btn-radius)">
            Get a Free Quote <i class="bi bi-arrow-right ms-2"></i>
          </a>
        </div>
      </div>
    </section>
  `,
  styles: [`
    .about-placeholder { width: 100%; height: 320px; background: var(--site-gradient); opacity: 0.1; display: flex; align-items: center; justify-content: center; font-size: 5rem; color: var(--site-primary); }
    .project-placeholder { height: 200px; background: var(--site-gradient); opacity: 0.1; display: flex; align-items: center; justify-content: center; font-size: 3rem; color: var(--site-primary); }
  `]
})
export class SiteHomePage implements OnInit {
  private siteService = inject(SiteService);

  settings: SiteSettings = DEFAULT_SITE;
  stats: Stat[] = [];
  services: Service[] = [];
  testimonials: Testimonial[] = [];
  blogs: BlogPost[] = [];
  pricing: PricingPlan[] = [];
  faqs: Faq[] = [];
  projects: any[] = [];

  ngOnInit(): void {
    this.siteService.getSettings().subscribe(s => this.settings = s);
    this.siteService.getStats().subscribe(s => this.stats = s);
    this.siteService.getServices().subscribe(s => this.services = s);
    this.siteService.getTestimonials().subscribe(t => this.testimonials = t);
    this.siteService.getBlogs().subscribe(b => this.blogs = b);
    this.siteService.getPricing().subscribe(p => this.pricing = p);
    this.siteService.getFaqs().subscribe(f => this.faqs = f);
    this.siteService.getProjects().subscribe(p => this.projects = p);
  }
}

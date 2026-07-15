import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import {
  WebsiteSettings, WebsiteContent, Faq, NavItem, WebsitePerson,
  PortalProject, PricingPlan, WebsiteService,
} from '../models/website-admin.model';

@Injectable({ providedIn: 'root' })
export class WebsiteAdminService {
  private readonly base = '/website/admin';

  constructor(private api: ApiService) {}

  // ---- Settings ----
  getSettings(): Observable<WebsiteSettings> {
    return this.api.get<WebsiteSettings>(`${this.base}/settings`);
  }
  saveSettings(payload: WebsiteSettings): Observable<WebsiteSettings> {
    return this.api.put<WebsiteSettings>(`${this.base}/settings`, payload);
  }

  // ---- Services ----
  listServices(): Observable<WebsiteService[]> {
    return this.api.get<WebsiteService[]>(`${this.base}/services`);
  }
  createService(payload: WebsiteService): Observable<WebsiteService> {
    return this.api.post<WebsiteService>(`${this.base}/services`, payload);
  }
  updateService(id: number, payload: WebsiteService): Observable<WebsiteService> {
    return this.api.put<WebsiteService>(`${this.base}/services/${id}`, payload);
  }
  deleteService(id: number): Observable<void> {
    return this.api.delete<void>(`${this.base}/services/${id}`);
  }

  // ---- Blog ----
  listBlogPosts(): Observable<WebsiteContent[]> {
    return this.api.get<WebsiteContent[]>(`${this.base}/blog`);
  }
  createBlogPost(payload: WebsiteContent): Observable<WebsiteContent> {
    return this.api.post<WebsiteContent>(`${this.base}/blog`, payload);
  }
  updateBlogPost(id: number, payload: WebsiteContent): Observable<WebsiteContent> {
    return this.api.put<WebsiteContent>(`${this.base}/blog/${id}`, payload);
  }
  deleteBlogPost(id: number): Observable<void> {
    return this.api.delete<void>(`${this.base}/blog/${id}`);
  }

  // ---- Pages ----
  listPages(): Observable<WebsiteContent[]> {
    return this.api.get<WebsiteContent[]>(`${this.base}/pages`);
  }
  savePage(payload: WebsiteContent): Observable<WebsiteContent> {
    return this.api.put<WebsiteContent>(`${this.base}/pages`, payload);
  }
  deletePage(id: number): Observable<void> {
    return this.api.delete<void>(`${this.base}/pages/${id}`);
  }

  // ---- FAQs ----
  listFaqs(): Observable<Faq[]> {
    return this.api.get<Faq[]>(`${this.base}/faqs`);
  }
  createFaq(payload: Faq): Observable<Faq> {
    return this.api.post<Faq>(`${this.base}/faqs`, payload);
  }
  updateFaq(id: number, payload: Faq): Observable<Faq> {
    return this.api.put<Faq>(`${this.base}/faqs/${id}`, payload);
  }
  deleteFaq(id: number): Observable<void> {
    return this.api.delete<void>(`${this.base}/faqs/${id}`);
  }

  // ---- Nav ----
  listNavItems(): Observable<NavItem[]> {
    return this.api.get<NavItem[]>(`${this.base}/nav`);
  }
  createNavItem(payload: NavItem): Observable<NavItem> {
    return this.api.post<NavItem>(`${this.base}/nav`, payload);
  }
  updateNavItem(id: number, payload: NavItem): Observable<NavItem> {
    return this.api.put<NavItem>(`${this.base}/nav/${id}`, payload);
  }
  deleteNavItem(id: number): Observable<void> {
    return this.api.delete<void>(`${this.base}/nav/${id}`);
  }

  // ---- Team ----
  listTeam(): Observable<WebsitePerson[]> {
    return this.api.get<WebsitePerson[]>(`${this.base}/team`);
  }
  createTeamMember(payload: WebsitePerson): Observable<WebsitePerson> {
    return this.api.post<WebsitePerson>(`${this.base}/team`, payload);
  }
  updateTeamMember(id: number, payload: WebsitePerson): Observable<WebsitePerson> {
    return this.api.put<WebsitePerson>(`${this.base}/team/${id}`, payload);
  }
  deleteTeamMember(id: number): Observable<void> {
    return this.api.delete<void>(`${this.base}/team/${id}`);
  }

  // ---- Testimonials ----
  listTestimonials(): Observable<WebsitePerson[]> {
    return this.api.get<WebsitePerson[]>(`${this.base}/testimonials`);
  }
  createTestimonial(payload: WebsitePerson): Observable<WebsitePerson> {
    return this.api.post<WebsitePerson>(`${this.base}/testimonials`, payload);
  }
  updateTestimonial(id: number, payload: WebsitePerson): Observable<WebsitePerson> {
    return this.api.put<WebsitePerson>(`${this.base}/testimonials/${id}`, payload);
  }
  deleteTestimonial(id: number): Observable<void> {
    return this.api.delete<void>(`${this.base}/testimonials/${id}`);
  }

  // ---- Projects ----
  listProjects(): Observable<PortalProject[]> {
    return this.api.get<PortalProject[]>(`${this.base}/projects`);
  }
  createProject(payload: PortalProject): Observable<PortalProject> {
    return this.api.post<PortalProject>(`${this.base}/projects`, payload);
  }
  updateProject(id: number, payload: PortalProject): Observable<PortalProject> {
    return this.api.put<PortalProject>(`${this.base}/projects/${id}`, payload);
  }
  deleteProject(id: number): Observable<void> {
    return this.api.delete<void>(`${this.base}/projects/${id}`);
  }

  // ---- Pricing ----
  listPricingPlans(): Observable<PricingPlan[]> {
    return this.api.get<PricingPlan[]>(`${this.base}/pricing`);
  }
  createPricingPlan(payload: PricingPlan): Observable<PricingPlan> {
    return this.api.post<PricingPlan>(`${this.base}/pricing`, payload);
  }
  updatePricingPlan(id: number, payload: PricingPlan): Observable<PricingPlan> {
    return this.api.put<PricingPlan>(`${this.base}/pricing/${id}`, payload);
  }
  deletePricingPlan(id: number): Observable<void> {
    return this.api.delete<void>(`${this.base}/pricing/${id}`);
  }
}

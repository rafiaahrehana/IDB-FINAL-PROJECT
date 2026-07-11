import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, switchMap, catchError } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  SiteSettings, NavItem, Service, BlogPost, Testimonial,
  Faq, TeamMember, Project, PricingPlan, Stat, CmsPage,
  ServiceRequestPayload, TrackedRequest
} from '../models/site.model';
import {
  DEFAULT_SITE, DEFAULT_SERVICES, DEFAULT_BLOGS, DEFAULT_TESTIMONIALS,
  DEFAULT_FAQS, DEFAULT_TEAM, DEFAULT_PROJECTS, DEFAULT_PRICING,
  DEFAULT_STATS, DEFAULT_PAGES
} from './default-site.config';

@Injectable({ providedIn: 'root' })
export class SiteService {
  private http = inject(HttpClient);
  private api = environment.apiUrl;

  private _subdomain = '';

  setSubdomain(value: string): void {
    this._subdomain = value;
  }

  private get subdomain(): string {
    if (this._subdomain) return this._subdomain;
    const host = window.location.hostname;
    const parts = host.split('.');
    if (parts.length > 2) return parts[0];
    return '';
  }

  private params(subdomain?: string): Record<string, string> {
    const s = subdomain || this.subdomain;
    return s ? { subdomain: s } : {};
  }

  getSettings(): Observable<SiteSettings> {
    return this.http.get<SiteSettings>(`${this.api}/website/settings`, { params: this.params() }).pipe(
      catchError(() => of(DEFAULT_SITE))
    );
  }

  getNav(): Observable<NavItem[]> {
    return this.http.get<NavItem[]>(`${this.api}/website/nav`, { params: this.params() }).pipe(
      catchError(() => of([]))
    );
  }

  getServices(category?: string, q?: string): Observable<Service[]> {
    const p: Record<string, string> = { ...this.params() };
    if (category) p['category'] = category;
    if (q) p['q'] = q;
    return this.http.get<Service[]>(`${this.api}/website/services`, { params: p }).pipe(
      catchError(() => of(DEFAULT_SERVICES))
    );
  }

  getService(slug: string): Observable<Service> {
    return this.http.get<Service>(`${this.api}/website/services/${slug}`, { params: this.params() }).pipe(
      catchError(() => of(DEFAULT_SERVICES[0]))
    );
  }

  getBlogs(category?: string): Observable<BlogPost[]> {
    const p: Record<string, string> = { ...this.params() };
    if (category) p['category'] = category;
    return this.http.get<BlogPost[]>(`${this.api}/website/blog`, { params: p }).pipe(
      catchError(() => of(DEFAULT_BLOGS))
    );
  }

  getBlog(slug: string): Observable<BlogPost> {
    return this.http.get<BlogPost>(`${this.api}/website/blog/${slug}`, { params: this.params() }).pipe(
      catchError(() => of(DEFAULT_BLOGS[0]))
    );
  }

  getTestimonials(): Observable<Testimonial[]> {
    return this.http.get<Testimonial[]>(`${this.api}/website/testimonials`, { params: this.params() }).pipe(
      catchError(() => of(DEFAULT_TESTIMONIALS))
    );
  }

  getFaqs(): Observable<Faq[]> {
    return this.http.get<Faq[]>(`${this.api}/website/faqs`, { params: this.params() }).pipe(
      catchError(() => of(DEFAULT_FAQS))
    );
  }

  getTeam(): Observable<TeamMember[]> {
    return this.http.get<TeamMember[]>(`${this.api}/website/team`, { params: this.params() }).pipe(
      catchError(() => of(DEFAULT_TEAM))
    );
  }

  getProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.api}/website/projects`, { params: this.params() }).pipe(
      catchError(() => of(DEFAULT_PROJECTS))
    );
  }

  getPricing(): Observable<PricingPlan[]> {
    return this.http.get<PricingPlan[]>(`${this.api}/website/pricing`, { params: this.params() }).pipe(
      catchError(() => of(DEFAULT_PRICING))
    );
  }

  getStats(): Observable<Stat[]> {
    return this.http.get<Stat[]>(`${this.api}/website/stats`, { params: this.params() }).pipe(
      catchError(() => of(DEFAULT_STATS))
    );
  }

  getPage(slug: string): Observable<CmsPage> {
    return this.http.get<CmsPage>(`${this.api}/website/pages/${slug}`, { params: this.params() }).pipe(
      catchError(() => of(DEFAULT_PAGES[slug] || { slug, title: slug, body: '', updatedAt: '' }))
    );
  }

  submitContact(body: { name: string; email: string; phone?: string; subject?: string; message: string }): Observable<void> {
    return this.http.post<void>(`${this.api}/website/contact`, body, { params: this.params() }).pipe(
      switchMap(() => of(void 0)),
      catchError(() => of(void 0))
    );
  }

  submitNewsletter(email: string): Observable<void> {
    return this.http.post<void>(`${this.api}/website/newsletter`, { email }, { params: this.params() }).pipe(
      switchMap(() => of(void 0)),
      catchError(() => of(void 0))
    );
  }

  submitServiceRequest(payload: ServiceRequestPayload): Observable<{ code: string }> {
    return this.http.post<{ code: string }>(`${this.api}/website/service-requests`, payload, { params: this.params() }).pipe(
      catchError(() => of({ code: '' }))
    );
  }

  trackRequest(code: string): Observable<TrackedRequest | null> {
    return this.http.get<TrackedRequest>(`${this.api}/website/service-requests/track/${code}`, { params: this.params() }).pipe(
      catchError(() => of(null))
    );
  }
}

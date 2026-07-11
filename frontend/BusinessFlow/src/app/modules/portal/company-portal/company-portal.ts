import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CompanyPublic, PortalService } from '../portal.service';
import { CompanyService as HubService } from '../../servicedesk/models/servicedesk.model';
import { Loader } from '../../../shared/components/loader/loader';

// The real BusinessOS request lifecycle - shown on the portal so clients know
// what happens after they submit. Order carries meaning; hence numbered.
const PROCESS_STEPS = [
  { icon: 'bi-send', title: 'Request', text: 'Pick a service and fill in its form' },
  { icon: 'bi-file-earmark-text', title: 'Quotation', text: 'Receive and approve the price' },
  { icon: 'bi-person-check', title: 'Assigned', text: 'A specialist takes your case' },
  { icon: 'bi-arrow-repeat', title: 'In progress', text: 'Track every stage live' },
  { icon: 'bi-search', title: 'Review', text: 'Work is checked before delivery' },
  { icon: 'bi-check2-circle', title: 'Done', text: 'Delivered, with your documents' },
];

@Component({
  selector: 'app-company-portal',
  imports: [CommonModule, RouterLink, Loader],
  templateUrl: './company-portal.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './company-portal.scss',
})
export class CompanyPortal implements OnInit {
  company?: CompanyPublic;
  services: HubService[] = [];
  loading = false;
  notFound = false;

  steps = PROCESS_STEPS;
  year = new Date().getFullYear();

  constructor(private route: ActivatedRoute, private portalService: PortalService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    // Subscribe (not snapshot) so navigating between portals re-loads
    this.route.paramMap.subscribe((params) => {
      const subdomain = params.get('subdomain');
      if (subdomain) this.load(subdomain);
    });
  }

  private load(subdomain: string): void {
    this.loading = true;
    this.notFound = false;
    this.portalService.getPublic(subdomain).subscribe({
      next: (c) => {
        this.company = c;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.notFound = true;
        this.loading = false;
      },
    });
    this.portalService.getPublicServices(subdomain).subscribe({
      // Featured services first, then by name - the owner's own ordering signal
      next: (s) => (this.services = [...s].sort(
        (a, b) => Number(b.featured) - Number(a.featured) || a.name.localeCompare(b.name))),
      error: () => (this.services = []),
    });
  }

  // The whole page derives its accent palette from the company's two brand
  // colors via CSS custom properties - this is what makes each tenant's
  // portal unmistakably theirs.
  get brandVars(): Record<string, string> {
    return {
      '--brand': this.company?.primaryColor || '#2563eb',
      '--brand-2': this.company?.secondaryColor || '#475569',
    };
  }

  // Delivery mode chips shown only when the flag is meaningful
  modes(s: HubService): string[] {
    const m: string[] = [];
    if (s.remote) m.push('Remote');
    if (s.onSite) m.push('On-site');
    if (s.online) m.push('Online');
    return m;
  }
}

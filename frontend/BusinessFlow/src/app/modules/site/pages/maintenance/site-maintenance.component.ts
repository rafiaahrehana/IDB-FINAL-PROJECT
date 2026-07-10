import { Component, OnInit, inject } from '@angular/core';
import { SiteService } from '../../services/site.service';
import { SiteSettings } from '../../models/site.model';
import { DEFAULT_SITE } from '../../services/default-site.config';

@Component({
  selector: 'app-site-home',
  standalone: true,
  template: `
    <section class="d-flex align-items-center text-center" style="min-height: 80vh; background: var(--site-gradient); color: #fff">
      <div class="container">
        <h1 class="display-3 fw-bold mb-3">Maintenance Mode</h1>
        <p class="lead mb-0 opacity-75">We're currently performing scheduled maintenance. We'll be back soon.</p>
      </div>
    </section>
  `
})
export class SiteMaintenanceComponent {}

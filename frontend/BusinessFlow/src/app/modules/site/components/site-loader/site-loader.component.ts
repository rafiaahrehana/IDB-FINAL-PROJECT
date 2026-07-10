import { Component } from '@angular/core';

@Component({
  selector: 'app-site-loader',
  standalone: true,
  template: `
    <div class="loader-wrap d-flex align-items-center justify-content-center py-5">
      <div class="spinner-border" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
  `,
  styles: [`
    .spinner-border { width: 2.5rem; height: 2.5rem; color: var(--site-primary); }
  `]
})
export class SiteLoaderComponent {}

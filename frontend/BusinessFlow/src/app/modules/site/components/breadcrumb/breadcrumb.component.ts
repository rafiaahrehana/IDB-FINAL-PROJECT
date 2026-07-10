import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-breadcrumb',
  standalone: true,
  imports: [RouterLink],
  template: `
    <nav aria-label="breadcrumb" class="py-3">
      <ol class="breadcrumb mb-0">
        <li class="breadcrumb-item"><a routerLink="/" class="text-decoration-none">Home</a></li>
        @for (item of items; track item.label; let last = $last) {
          @if (last) {
            <li class="breadcrumb-item active" aria-current="page">{{ item.label }}</li>
          } @else {
            <li class="breadcrumb-item"><a [href]="item.url" class="text-decoration-none">{{ item.label }}</a></li>
          }
        }
      </ol>
    </nav>
  `,
  styles: [`
    .breadcrumb { background: transparent; }
    a { color: var(--site-primary); }
  `]
})
export class BreadcrumbComponent {
  @Input() items: { label: string; url?: string }[] = [];
}

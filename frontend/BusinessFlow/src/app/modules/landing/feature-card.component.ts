import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-feature-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="feature-card h-100">
      <div class="feature-icon"><i class="bi {{ icon }}"></i></div>
      <h3 class="feature-title">{{ title }}</h3>
      <p class="feature-desc">{{ desc }}</p>
    </div>
  `,
  styles: [`
    .feature-card {
      background: #fff;
      border: 1px solid var(--bos-border);
      border-radius: 14px;
      padding: 1.75rem;
      transition: transform .25s ease, box-shadow .25s ease, border-color .25s ease;
    }
    .feature-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 18px 40px -24px rgba(15, 23, 42, .25);
      border-color: #cdd9ec;
    }
    .feature-icon {
      width: 48px; height: 48px;
      display: flex; align-items: center; justify-content: center;
      border-radius: 12px;
      background: rgba(37, 99, 235, .1);
      color: var(--bos-primary);
      font-size: 1.35rem;
      margin-bottom: 1rem;
    }
    .feature-title { font-size: 1.1rem; font-weight: 700; color: var(--bos-dark); margin-bottom: .5rem; font-family: 'Manrope', sans-serif; }
    .feature-desc { font-size: .95rem; color: var(--bos-muted); margin: 0; line-height: 1.6; }
  `],
})
export class FeatureCard {
  @Input() icon = 'bi-stars';
  @Input() title = '';
  @Input() desc = '';
}

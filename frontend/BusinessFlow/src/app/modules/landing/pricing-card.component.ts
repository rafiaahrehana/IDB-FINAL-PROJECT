import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-pricing-card',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="pricing-card h-100" [class.featured]="featured">
      @if (featured) { <span class="badge-popular">Most Popular</span> }
      <h3 class="plan-name">{{ plan.name }}</h3>
      <p class="plan-desc">{{ plan.description }}</p>
      <div class="plan-price">
        <span class="amount">{{ plan.price }}</span>
        <span class="period">{{ plan.period }}</span>
      </div>
      <a class="btn w-100" [class.btn-primary]="featured" [class.btn-outline-primary]="!featured"
         [routerLink]="plan.ctaLink">{{ plan.cta }}</a>
      <ul class="plan-features">
        @for (f of plan.features; track f) {
          <li><i class="bi bi-check2"></i><span>{{ f }}</span></li>
        }
      </ul>
    </div>
  `,
  styles: [`
    .pricing-card {
      position: relative;
      background: #fff;
      border: 1px solid var(--bos-border);
      border-radius: 18px;
      padding: 2rem 1.75rem;
      transition: transform .25s ease, box-shadow .25s ease;
    }
    .pricing-card.featured {
      border-color: var(--bos-primary);
      box-shadow: 0 30px 60px -35px rgba(37, 99, 235, .5);
      transform: translateY(-6px);
    }
    .badge-popular {
      position: absolute; top: -12px; left: 50%; transform: translateX(-50%);
      background: var(--bos-primary); color: #fff;
      font-size: .72rem; font-weight: 700; letter-spacing: .03em;
      padding: .3rem .8rem; border-radius: 999px;
    }
    .plan-name { font-size: 1.2rem; font-weight: 800; color: var(--bos-dark); font-family: 'Manrope', sans-serif; }
    .plan-desc { font-size: .9rem; color: var(--bos-muted); min-height: 2.4rem; }
    .plan-price { margin: 1rem 0 1.25rem; }
    .plan-price .amount { font-size: 2.4rem; font-weight: 800; color: var(--bos-dark); font-family: 'Manrope', sans-serif; }
    .plan-price .period { font-size: .9rem; color: var(--bos-muted); margin-left: .25rem; }
    .plan-features { list-style: none; padding: 0; margin: 1.5rem 0 0; }
    .plan-features li { display: flex; gap: .6rem; align-items: flex-start; padding: .45rem 0; font-size: .92rem; color: var(--bos-text); }
    .plan-features i { color: var(--bos-success); font-size: 1.05rem; margin-top: .1rem; }
  `],
})
export class PricingCard {
  @Input() plan!: {
    name: string; description: string; price: string; period: string;
    cta: string; ctaLink: string; features: string[];
  };
  @Input() featured = false;
}

import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-testimonial-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="testimonial-card h-100">
      <div class="stars">
        @for (s of stars; track s) { <i class="bi bi-star-fill"></i> }
      </div>
      <p class="quote">"{{ data.quote }}"</p>
      <div class="person">
        <div class="avatar" [style.background]="data.color">{{ data.initials }}</div>
        <div>
          <div class="name">{{ data.name }}</div>
          <div class="role">{{ data.role }}</div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .testimonial-card {
      background: #fff; border: 1px solid var(--bos-border);
      border-radius: 16px; padding: 1.75rem;
      transition: transform .25s ease, box-shadow .25s ease;
    }
    .testimonial-card:hover { transform: translateY(-4px); box-shadow: 0 18px 40px -26px rgba(15,23,42,.25); }
    .stars { color: #F59E0B; font-size: .85rem; margin-bottom: .75rem; }
    .quote { font-size: .98rem; color: var(--bos-text); line-height: 1.65; margin-bottom: 1.25rem; font-family: 'Plus Jakarta Sans', sans-serif; }
    .person { display: flex; align-items: center; gap: .75rem; }
    .avatar {
      width: 44px; height: 44px; border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      color: #fff; font-weight: 700; font-size: .9rem;
    }
    .name { font-weight: 700; color: var(--bos-dark); font-size: .95rem; }
    .role { font-size: .82rem; color: var(--bos-muted); }
  `],
})
export class TestimonialCard {
  @Input() data!: {
    name: string; role: string; quote: string; initials: string; color: string;
  };
  stars = [1, 2, 3, 4, 5];
}

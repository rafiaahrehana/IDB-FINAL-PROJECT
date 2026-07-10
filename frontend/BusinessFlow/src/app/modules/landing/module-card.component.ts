import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-module-card',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <a class="module-card h-100" [routerLink]="link">
      <div class="module-icon" [style.background]="accentSoft" [style.color]="accent">
        <i class="bi {{ icon }}"></i>
      </div>
      <h3 class="module-title">{{ title }}</h3>
      <p class="module-desc">{{ desc }}</p>
      <span class="module-link">Learn more <i class="bi bi-arrow-right"></i></span>
    </a>
  `,
  styles: [`
    .module-card {
      display: block;
      background: #fff;
      border: 1px solid var(--bos-border);
      border-radius: 16px;
      padding: 1.5rem;
      text-decoration: none;
      transition: transform .25s ease, box-shadow .25s ease, border-color .25s ease;
    }
    .module-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 22px 45px -28px rgba(15, 23, 42, .3);
      border-color: #cdd9ec;
    }
    .module-icon {
      width: 46px; height: 46px;
      display: flex; align-items: center; justify-content: center;
      border-radius: 12px;
      font-size: 1.3rem;
      margin-bottom: .9rem;
    }
    .module-title { font-size: 1.05rem; font-weight: 700; color: var(--bos-dark); margin-bottom: .4rem; font-family: 'Manrope', sans-serif; }
    .module-desc { font-size: .9rem; color: var(--bos-muted); margin: 0 0 1rem; line-height: 1.55; }
    .module-link { font-size: .85rem; font-weight: 600; color: var(--bos-primary); }
    .module-card:hover .module-link i { transform: translateX(3px); }
    .module-link i { display: inline-block; transition: transform .2s ease; }
  `],
})
export class ModuleCard {
  @Input() icon = 'bi-grid';
  @Input() title = '';
  @Input() desc = '';
  @Input() link = '#';
  @Input() accent = '#2563EB';
  get accentSoft(): string { return this.accent + '1a'; }
}

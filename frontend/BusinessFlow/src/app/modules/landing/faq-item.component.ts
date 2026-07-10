import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-faq-item',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="faq-item" [class.open]="open">
      <button class="faq-q" (click)="open = !open" [attr.aria-expanded]="open">
        <span>{{ question }}</span>
        <i class="bi" [class.bi-plus-lg]="!open" [class.bi-dash-lg]="open"></i>
      </button>
      @if (open) {
        <div class="faq-a"><p>{{ answer }}</p></div>
      }
    </div>
  `,
  styles: [`
    .faq-item {
      background: #fff; border: 1px solid var(--bos-border);
      border-radius: 12px; margin-bottom: .75rem; overflow: hidden;
      transition: border-color .2s ease;
    }
    .faq-item.open { border-color: #c7d2e6; }
    .faq-q {
      width: 100%; display: flex; justify-content: space-between; align-items: center;
      gap: 1rem; padding: 1.1rem 1.25rem;
      background: none; border: 0; cursor: pointer;
      font-weight: 600; color: var(--bos-dark); font-size: .98rem; text-align: left;
      font-family: 'Manrope', sans-serif;
    }
    .faq-q i { color: var(--bos-primary); font-size: 1.1rem; flex-shrink: 0; }
    .faq-a { padding: 0 1.25rem 1.1rem; }
    .faq-a p { margin: 0; color: var(--bos-muted); font-size: .92rem; line-height: 1.65; }
  `],
})
export class FaqItem {
  @Input() question = '';
  @Input() answer = '';
  open = false;
}

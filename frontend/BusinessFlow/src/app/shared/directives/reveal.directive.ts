import { Directive, ElementRef, OnDestroy, OnInit } from '@angular/core';

/**
 * Adds a subtle fade-up reveal when the element scrolls into view.
 * Usage: <section appReveal> ... paired with the .reveal styles in the page scss.
 * Respects prefers-reduced-motion (elements simply stay visible).
 */
@Directive({
  selector: '[appReveal]',
})
export class RevealDirective implements OnInit, OnDestroy {
  private observer?: IntersectionObserver;

  constructor(private el: ElementRef<HTMLElement>) {}

  ngOnInit(): void {
    const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (reduced || !('IntersectionObserver' in window)) return;

    this.el.nativeElement.classList.add('reveal');
    this.observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            this.el.nativeElement.classList.add('reveal-in');
            this.observer?.disconnect();
          }
        }
      },
      { threshold: 0.12 },
    );
    this.observer.observe(this.el.nativeElement);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }
}

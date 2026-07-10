import { Directive, ElementRef, OnDestroy, OnInit, Input, inject } from '@angular/core';

@Directive({
  selector: '[appReveal]',
  standalone: true,
})
export class RevealDirective implements OnInit, OnDestroy {
  @Input() revealDelay = 0;

  private observer?: IntersectionObserver;
  private el = inject(ElementRef<HTMLElement>);

  ngOnInit(): void {
    const node = this.el.nativeElement;
    node.classList.add('reveal');
    if (this.revealDelay) {
      node.style.transitionDelay = `${this.revealDelay}ms`;
    }
    if (typeof IntersectionObserver === 'undefined') {
      node.classList.add('reveal-visible');
      return;
    }
    this.observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            node.classList.add('reveal-visible');
            this.observer?.unobserve(node);
          }
        }
      },
      { threshold: 0.12, rootMargin: '0px 0px -40px 0px' },
    );
    this.observer.observe(node);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }
}

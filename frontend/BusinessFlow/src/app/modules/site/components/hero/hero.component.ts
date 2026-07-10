import { Component, Input, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SiteSettings } from '../../models/site.model';

@Component({
  selector: 'app-site-hero',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="site-hero d-flex align-items-center position-relative overflow-hidden">
      <div class="hero-bg"></div>
      <div class="container position-relative" style="z-index: 2">
        <div class="row align-items-center">
          <div class="col-lg-6 mb-5 mb-lg-0">
            <h1 class="display-4 fw-bold mb-4">{{ settings?.heroHeading || 'Welcome' }}</h1>
            <p class="lead mb-4" [style.max-width]="'540px'">{{ settings?.heroSubheading }}</p>
            <div class="d-flex gap-3 flex-wrap">
              <a routerLink="/request-service" class="btn btn-primary btn-lg px-5"
                 style="border-radius: var(--site-btn-radius)">
                Get Started <i class="bi bi-arrow-right ms-2"></i>
              </a>
              <a routerLink="/about" class="btn btn-outline-dark btn-lg px-5"
                 style="border-radius: var(--site-btn-radius)">
                Learn More
              </a>
            </div>
          </div>
          <div class="col-lg-6 text-center">
            @if (settings?.heroImages?.length) {
              <div class="hero-slider position-relative">
                @for (img of settings.heroImages; track img; let i = $index) {
                  <img [src]="img" [class.active]="i === currentSlide"
                       class="hero-img" [alt]="settings.companyName">
                }
              </div>
            } @else if (settings?.heroImageUrl) {
              <img [src]="settings.heroImageUrl" class="hero-img" [alt]="settings.companyName">
            } @else {
              <div class="hero-placeholder">
                <i class="bi bi-rocket-takeoff"></i>
              </div>
            }
          </div>
        </div>
      </div>
    </section>
  `,
  styles: [`
    .site-hero { min-height: 100vh; padding: 120px 0 80px; }
    .hero-bg { position: absolute; inset: 0; background: var(--site-gradient); opacity: 0.04; }
    .hero-img { max-height: 420px; border-radius: var(--site-radius); box-shadow: 0 20px 60px rgba(0,0,0,0.15); opacity: 0; transition: opacity 0.6s; width: 100%; object-fit: cover; }
    .hero-img.active { opacity: 1; }
    .hero-placeholder { width: 100%; max-width: 420px; height: 320px; margin: 0 auto; background: var(--site-gradient); opacity: 0.1; border-radius: var(--site-radius); display: flex; align-items: center; justify-content: center; font-size: 5rem; color: var(--site-primary); }
    .btn-primary { background: var(--site-primary); border-color: var(--site-primary); }
    .btn-primary:hover { background: var(--site-secondary); border-color: var(--site-secondary); }
    .btn-outline-dark { border-color: var(--site-primary); color: var(--site-primary); }
    .btn-outline-dark:hover { background: var(--site-primary); color: #fff; }
  `]
})
export class SiteHeroComponent implements OnInit {
  @Input() settings: SiteSettings | null = null;
  currentSlide = 0;
  private interval: any;

  ngOnInit(): void {
    if ((this.settings?.heroImages?.length || 0) > 1) {
      this.interval = setInterval(() => {
        this.currentSlide = (this.currentSlide + 1) % (this.settings!.heroImages.length);
      }, 4000);
    }
  }
}

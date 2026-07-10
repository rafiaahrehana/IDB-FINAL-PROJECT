import { Component, HostListener, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-landing-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <header class="landing-nav" [class.scrolled]="scrolled()">
      <div class="container">
        <nav class="nav-inner">
          <a class="brand" routerLink="/home">
            <span class="brand-mark"><i class="bi bi-intersect"></i></span>
            <span class="brand-name">BusinessOS</span>
          </a>

          <ul class="nav-links d-none d-lg-flex">
            <li><a href="#features">Features</a></li>
            <li><a href="#solutions">Solutions</a></li>
            <li><a href="#modules">Modules</a></li>
            <li><a href="#pricing">Pricing</a></li>
            <li><a href="#resources">Resources</a></li>
            <li><a href="#contact">Contact</a></li>
          </ul>

          <div class="nav-actions">
            <a class="btn btn-ghost" routerLink="/auth/login">Login</a>
            <a class="btn btn-primary" routerLink="/auth/register">Get Started</a>
            <button class="burger d-lg-none" (click)="mobileOpen.set(!mobileOpen())" aria-label="Menu">
              <i class="bi" [class.bi-list]="!mobileOpen()" [class.bi-x]="mobileOpen()"></i>
            </button>
          </div>
        </nav>

        @if (mobileOpen()) {
          <ul class="mobile-links d-lg-none">
            <li><a href="#features" (click)="mobileOpen.set(false)">Features</a></li>
            <li><a href="#solutions" (click)="mobileOpen.set(false)">Solutions</a></li>
            <li><a href="#modules" (click)="mobileOpen.set(false)">Modules</a></li>
            <li><a href="#pricing" (click)="mobileOpen.set(false)">Pricing</a></li>
            <li><a href="#resources" (click)="mobileOpen.set(false)">Resources</a></li>
            <li><a href="#contact" (click)="mobileOpen.set(false)">Contact</a></li>
            <li class="d-flex gap-2 mt-2">
              <a class="btn btn-ghost flex-fill" routerLink="/auth/login" (click)="mobileOpen.set(false)">Login</a>
              <a class="btn btn-primary flex-fill" routerLink="/auth/register" (click)="mobileOpen.set(false)">Get Started</a>
            </li>
          </ul>
        }
      </div>
    </header>
  `,
  styles: [`
    .landing-nav {
      position: fixed; top: 0; left: 0; right: 0; z-index: 1030;
      transition: background .25s ease, box-shadow .25s ease, border-color .25s ease;
      background: transparent; border-bottom: 1px solid transparent;
    }
    .landing-nav.scrolled {
      background: rgba(255, 255, 255, .85);
      backdrop-filter: saturate(180%) blur(12px);
      border-bottom: 1px solid var(--bos-border);
      box-shadow: 0 6px 24px -20px rgba(15, 23, 42, .4);
    }
    .nav-inner { display: flex; align-items: center; justify-content: space-between; height: 68px; }
    .brand { display: flex; align-items: center; gap: .55rem; text-decoration: none; }
    .brand-mark {
      width: 34px; height: 34px; border-radius: 9px;
      background: var(--bos-primary); color: #fff;
      display: flex; align-items: center; justify-content: center; font-size: 1.1rem;
    }
    .brand-name { font-weight: 800; font-size: 1.15rem; color: var(--bos-dark); font-family: 'Manrope', sans-serif; }
    .nav-links { list-style: none; display: flex; gap: 1.75rem; margin: 0; padding: 0; }
    .nav-links a {
      text-decoration: none; color: var(--bos-text); font-weight: 500; font-size: .95rem;
      transition: color .2s ease;
    }
    .nav-links a:hover { color: var(--bos-primary); }
    .nav-actions { display: flex; align-items: center; gap: .6rem; }
    .btn-ghost {
      background: transparent; color: var(--bos-dark); border: 1px solid transparent;
      font-weight: 600; padding: .5rem 1rem; border-radius: 9px; text-decoration: none;
    }
    .btn-ghost:hover { background: rgba(15, 23, 42, .05); }
    .burger {
      background: none; border: 0; font-size: 1.4rem; color: var(--bos-dark); cursor: pointer;
    }
    .mobile-links {
      list-style: none; margin: 0; padding: .5rem 0 1rem; border-top: 1px solid var(--bos-border);
    }
    .mobile-links li { padding: .55rem .25rem; }
    .mobile-links a { text-decoration: none; color: var(--bos-text); font-weight: 600; }
  `],
})
export class LandingNavbar {
  scrolled = signal(false);
  mobileOpen = signal(false);

  @HostListener('window:scroll')
  onScroll(): void {
    this.scrolled.set(window.scrollY > 24);
  }
}

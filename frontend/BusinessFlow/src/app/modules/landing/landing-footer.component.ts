import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-landing-footer',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <footer class="landing-footer">
      <div class="container">
        <div class="row gy-4">
          <div class="col-lg-3 col-md-6">
            <a class="brand" routerLink="/home">
              <span class="brand-mark"><i class="bi bi-intersect"></i></span>
              <span class="brand-name">BusinessOS</span>
            </a>
            <p class="about">The enterprise operations platform that connects HR, CRM, Finance, Service Desk and AI automation in one secure cloud.</p>
            <div class="socials">
              <a href="#" aria-label="Twitter"><i class="bi bi-twitter-x"></i></a>
              <a href="#" aria-label="LinkedIn"><i class="bi bi-linkedin"></i></a>
              <a href="#" aria-label="GitHub"><i class="bi bi-github"></i></a>
              <a href="#" aria-label="YouTube"><i class="bi bi-youtube"></i></a>
            </div>
          </div>

          <div class="col-lg-2 col-6">
            <h4>Products</h4>
            <ul>
              <li><a href="#modules">CRM</a></li>
              <li><a href="#modules">HRM</a></li>
              <li><a href="#modules">Finance</a></li>
              <li><a href="#modules">Service Desk</a></li>
              <li><a href="#modules">AI Assistant</a></li>
            </ul>
          </div>

          <div class="col-lg-2 col-6">
            <h4>Resources</h4>
            <ul>
              <li><a href="#features">Features</a></li>
              <li><a href="#pricing">Pricing</a></li>
              <li><a href="#faq">FAQ</a></li>
              <li><a href="#resources">Blog</a></li>
              <li><a href="#resources">Docs</a></li>
            </ul>
          </div>

          <div class="col-lg-2 col-6">
            <h4>Company</h4>
            <ul>
              <li><a href="#contact">About</a></li>
              <li><a href="#contact">Careers</a></li>
              <li><a href="#contact">Contact</a></li>
              <li><a href="#contact">Partners</a></li>
            </ul>
          </div>

          <div class="col-lg-3 col-6">
            <h4>Support</h4>
            <ul>
              <li><a href="#contact">Help Center</a></li>
              <li><a href="#contact">Status</a></li>
              <li><a routerLink="/auth/login">Client Portal</a></li>
              <li><a href="#">Privacy</a></li>
              <li><a href="#">Terms</a></li>
            </ul>
          </div>
        </div>

        <div class="footer-bottom">
          <span>&copy; 2026 BusinessOS Inc. All rights reserved.</span>
          <span class="text-muted">Built for medium &amp; large enterprises.</span>
        </div>
      </div>
    </footer>
  `,
  styles: [`
    .landing-footer { background: var(--bos-dark); color: #cbd5e1; padding: 3.5rem 0 2rem; }
    .brand { display: inline-flex; align-items: center; gap: .55rem; text-decoration: none; margin-bottom: 1rem; }
    .brand-mark { width: 32px; height: 32px; border-radius: 8px; background: var(--bos-primary); color: #fff; display: flex; align-items: center; justify-content: center; }
    .brand-name { font-weight: 800; font-size: 1.1rem; color: #fff; font-family: 'Manrope', sans-serif; }
    .about { font-size: .9rem; line-height: 1.6; max-width: 280px; }
    .socials { display: flex; gap: .6rem; margin-top: 1rem; }
    .socials a { width: 36px; height: 36px; border-radius: 9px; background: rgba(255,255,255,.06); color: #cbd5e1; display: flex; align-items: center; justify-content: center; text-decoration: none; transition: background .2s ease, color .2s ease; }
    .socials a:hover { background: var(--bos-primary); color: #fff; }
    h4 { color: #fff; font-size: .95rem; font-weight: 700; margin-bottom: .9rem; font-family: 'Manrope', sans-serif; }
    ul { list-style: none; padding: 0; margin: 0; }
    ul li { margin-bottom: .55rem; }
    ul a { color: #cbd5e1; text-decoration: none; font-size: .9rem; transition: color .2s ease; }
    ul a:hover { color: #fff; }
    .footer-bottom {
      display: flex; justify-content: space-between; flex-wrap: wrap; gap: .5rem;
      margin-top: 2.5rem; padding-top: 1.5rem; border-top: 1px solid rgba(255,255,255,.08);
      font-size: .85rem;
    }
  `],
})
export class LandingFooter {}

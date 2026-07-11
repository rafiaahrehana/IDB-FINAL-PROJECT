import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

import { NavbarComponent } from './components/navbar/navbar';
import { HeroComponent } from './components/hero/hero';
import { TrustedCompaniesComponent } from './components/trusted-companies/trusted-companies';
import { WhyBusinessOsComponent } from './components/why-business-os/why-business-os';
import { ModulesComponent } from './components/modules/modules';
import { DashboardPreviewComponent } from './components/dashboard-preview/dashboard-preview';
import { AiFeaturesComponent } from './components/ai-features/ai-features';
import { WorkflowAutomationComponent } from './components/workflow-automation/workflow-automation';
import { SecurityComponent } from './components/security/security';
import { PricingComponent } from './components/pricing/pricing';
import { TestimonialsComponent } from './components/testimonials/testimonials';
import { FaqComponent } from './components/faq/faq';
import { CtaComponent } from './components/cta/cta';
import { FooterComponent } from './components/footer/footer';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [
    CommonModule,
    NavbarComponent,
    HeroComponent,
    TrustedCompaniesComponent,
    WhyBusinessOsComponent,
    ModulesComponent,
    DashboardPreviewComponent,
    AiFeaturesComponent,
    WorkflowAutomationComponent,
    SecurityComponent,
    PricingComponent,
    TestimonialsComponent,
    FaqComponent,
    CtaComponent,
    FooterComponent
  ],
  template: `
    <div class="landing-wrapper">
      <app-navbar></app-navbar>
      
      <main>
        <app-hero></app-hero>
        <app-trusted-companies></app-trusted-companies>
        <app-why-business-os></app-why-business-os>
        <app-modules></app-modules>
        <app-dashboard-preview></app-dashboard-preview>
        <app-ai-features></app-ai-features>
        <app-workflow-automation></app-workflow-automation>
        <app-security></app-security>
        <app-pricing></app-pricing>
        <app-testimonials></app-testimonials>
        <app-faq></app-faq>
        <app-cta></app-cta>
      </main>
      
      <app-footer></app-footer>
    </div>
  `,
  styles: [`
    .landing-wrapper {
      scroll-behavior: smooth;
      overflow-x: hidden;
    }
  `]
})
export class Landing {
}

import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';

interface Feature { icon: string; title: string; text: string; }
interface Plan { name: string; tagline: string; features: string[]; highlight: boolean; }
interface Faq { q: string; a: string; }

@Component({
  selector: 'app-landing',
  imports: [RouterLink],
  templateUrl: './landing.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './landing.scss',
})
export class Landing {
  year = new Date().getFullYear();

  features: Feature[] = [
    { icon: 'bi-people', title: 'CRM', text: 'Leads, pipeline and accounts - track every deal from first contact to close.' },
    { icon: 'bi-clipboard-check', title: 'Service Desk', text: 'Custom request forms, workflows, quotations, SLAs and a knowledge base.' },
    { icon: 'bi-cash-stack', title: 'Finance', text: 'Invoices, expenses, ledger, wallet and reports - your numbers in one place.' },
    { icon: 'bi-person-badge', title: 'HRM & Attendance', text: 'Employees, roles, biometric check-in and timesheets.' },
    { icon: 'bi-box-seam', title: 'Assets & Inventory', text: 'Hardware, software licenses, onboarding and offboarding.' },
    { icon: 'bi-stars', title: 'AI Assistant', text: 'Built-in AI insights, recommendations and a company-aware assistant.' },
  ];

  // Plan names mirror the backend SubscriptionPlan enum. There is no public
  // price list in the system, so tiers link to registration / sales instead
  // of showing invented prices.
  plans: Plan[] = [
    { name: 'Free', tagline: 'Try BusinessOS with your team', highlight: false,
      features: ['Core modules', 'Single company', 'Community support'] },
    { name: 'Starter', tagline: 'For small teams getting organized', highlight: false,
      features: ['Everything in Free', 'Client portal', 'Email support'] },
    { name: 'Pro', tagline: 'For growing businesses', highlight: true,
      features: ['Everything in Starter', 'Workflows & automations', 'Priority support'] },
    { name: 'Enterprise', tagline: 'For organizations at scale', highlight: false,
      features: ['Everything in Pro', 'Dedicated success manager', 'Custom agreements'] },
  ];

  faqs: Faq[] = [
    { q: 'What is BusinessOS?', a: 'A multi-tenant business platform: CRM, service desk, finance, HR, assets and AI in one system, with a portal for your clients.' },
    { q: 'How do I get started?', a: 'Register your company for free. You get a workspace, can invite employees, publish services, and take client requests right away.' },
    { q: 'Do my clients need accounts?', a: 'Clients register once and can then browse your services, submit requests with your custom forms, approve quotations and track progress.' },
    { q: 'Can I brand my portal?', a: 'Yes - set your logo, colors, tagline and an about section, and share your own portal page with clients.' },
    { q: 'How does pricing work?', a: 'Start on the Free plan. Upgrade to Starter, Pro or Enterprise as you grow - contact us for plan details.' },
  ];
  openFaq: number | null = 0;

  toggleFaq(i: number): void {
    this.openFaq = this.openFaq === i ? null : i;
  }
}

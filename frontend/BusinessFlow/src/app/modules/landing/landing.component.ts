import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { RevealDirective } from './reveal.directive';
import { LandingNavbar } from './landing-navbar.component';
import { LandingFooter } from './landing-footer.component';
import { FeatureCard } from './feature-card.component';
import { ModuleCard } from './module-card.component';
import { PricingCard } from './pricing-card.component';
import { TestimonialCard } from './testimonial-card.component';
import { FaqItem } from './faq-item.component';

interface Module { icon: string; title: string; desc: string; link: string; accent: string; }
interface Item { icon: string; title: string; desc: string; }
interface Plan { name: string; description: string; price: string; period: string; cta: string; ctaLink: string; features: string[]; }
interface Testimonial { name: string; role: string; quote: string; initials: string; color: string; }
interface Tab { id: string; label: string; icon: string; }

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [
    CommonModule, RouterLink,
    RevealDirective, LandingNavbar, LandingFooter,
    FeatureCard, ModuleCard, PricingCard, TestimonialCard, FaqItem,
  ],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss',
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class LandingPage {
  activeTab = 'crm';

  modules: Module[] = [
    { icon: 'bi-people', title: 'CRM', desc: 'Manage leads, pipelines, accounts and deals with a 360° customer view.', link: '/crm/leads', accent: '#2563EB' },
    { icon: 'bi-person-badge', title: 'HRM', desc: 'Employees, departments, payroll, leaves and performance in one place.', link: '/hrm/employees', accent: '#14B8A6' },
    { icon: 'bi-cash-coin', title: 'Finance', desc: 'Invoices, expenses, ledger, wallets and real-time financial reports.', link: '/finance/invoices', accent: '#22C55E' },
    { icon: 'bi-box-seam', title: 'Inventory', desc: 'Track stock, warehouses, transfers and procurement end to end.', link: '/itam/hardware', accent: '#F59E0B' },
    { icon: 'bi-bag-check', title: 'Procurement', desc: 'Purchase requests, approvals, vendors and purchase orders.', link: '/finance/expenses', accent: '#8B5CF6' },
    { icon: 'bi-headset', title: 'Service Desk', desc: 'Tickets, SLAs, knowledge base and client request workflows.', link: '/servicedesk/requests', accent: '#EF4444' },
    { icon: 'bi-life-preserver', title: 'Support', desc: 'Omnichannel support, agents, categories and SLA policies.', link: '/support/tickets', accent: '#0EA5E9' },
    { icon: 'bi-stars', title: 'AI Assistant', desc: 'Generative AI for summaries, drafts, insights and automation.', link: '/ai', accent: '#6366F1' },
    { icon: 'bi-file-earmark-text', title: 'Documents', desc: 'Centralised documents, templates and client file sharing.', link: '/hrm/documents', accent: '#0D9488' },
    { icon: 'bi-diagram-2', title: 'Workflow Engine', desc: 'Automate approvals, tasks and cross-module business processes.', link: '/servicedesk/workflows', accent: '#DB2777' },
    { icon: 'bi-bar-chart', title: 'Analytics', desc: 'Dashboards and insights across every business function.', link: '/dashboard', accent: '#2563EB' },
    { icon: 'bi-bell', title: 'Notifications', desc: 'Real-time alerts, preferences and multi-channel delivery.', link: '/notifications', accent: '#F59E0B' },
  ];

  why: Item[] = [
    { icon: 'bi-building', title: 'Multi-Tenant SaaS', desc: 'Each company gets an isolated, secure workspace under its own subdomain.' },
    { icon: 'bi-cpu', title: 'AI Powered', desc: 'Embedded AI assists with summaries, drafts, insights and smart suggestions.' },
    { icon: 'bi-shield-lock', title: 'Enterprise Security', desc: 'JWT auth, RBAC, audit logs and encrypted, tenant-isolated storage.' },
    { icon: 'bi-arrow-repeat', title: 'Workflow Automation', desc: 'Turn manual processes into automated, trackable workflows.' },
    { icon: 'bi-cloud-check', title: 'Cloud Native', desc: 'Scalable, always-available infrastructure with zero maintenance.' },
    { icon: 'bi-graph-up-arrow', title: 'Scalable Architecture', desc: 'From fast-growing startups to large enterprises with thousands of users.' },
  ];

  aiFeatures: Item[] = [
    { icon: 'bi-chat-square-text', title: 'AI Chat', desc: 'Ask questions about your business in natural language.' },
    { icon: 'bi-file-earmark-medical', title: 'Document Analysis', desc: 'Summarise contracts, reports and client documents instantly.' },
    { icon: 'bi-envelope-paper', title: 'Email Generation', desc: 'Draft client and internal emails in your tone of voice.' },
    { icon: 'bi-pie-chart', title: 'Business Reports', desc: 'Generate insights from live dashboard data on demand.' },
    { icon: 'bi-lightbulb', title: 'Smart Suggestions', desc: 'Get recommended next steps from your pipeline and tickets.' },
    { icon: 'bi-robot', title: 'Workflow Automation', desc: 'Let AI trigger and route work across modules.' },
  ];

  security: Item[] = [
    { icon: 'bi-key', title: 'JWT Authentication', desc: 'Stateless, signed tokens with secure refresh handling.' },
    { icon: 'bi-shield-check', title: 'Role Based Access', desc: 'Granular roles and permissions per company and user.' },
    { icon: 'bi-journal-check', title: 'Audit Logs', desc: 'Immutable records of every significant action.' },
    { icon: 'bi-lock', title: 'Encrypted Storage', desc: 'Data encrypted in transit and at rest.' },
    { icon: 'bi-boxes', title: 'Tenant Isolation', desc: 'Hard separation of data between companies.' },
    { icon: 'bi-shield-lock', title: 'Secure APIs', desc: 'Authenticated, rate-aware and guarded endpoints.' },
  ];

  tabs: Tab[] = [
    { id: 'crm', label: 'CRM', icon: 'bi-people' },
    { id: 'hrm', label: 'HRM', icon: 'bi-person-badge' },
    { id: 'finance', label: 'Finance', icon: 'bi-cash-coin' },
    { id: 'inventory', label: 'Inventory', icon: 'bi-box-seam' },
    { id: 'support', label: 'Support', icon: 'bi-life-preserver' },
  ];

  pricing: Plan[] = [
    {
      name: 'Starter', description: 'For small teams getting organised.',
      price: '$19', period: '/user / mo', cta: 'Start Free Trial', ctaLink: '/auth/register',
      features: ['Up to 10 users', 'Core CRM & HRM', 'Service Desk', 'Email support', '1 GB storage'],
    },
    {
      name: 'Professional', description: 'For scaling companies that need automation.',
      price: '$49', period: '/user / mo', cta: 'Start Free Trial', ctaLink: '/auth/register',
      features: ['Unlimited users', 'All modules', 'Workflow engine', 'AI Assistant', 'Priority support', '100 GB storage'],
    },
    {
      name: 'Enterprise', description: 'For large organisations with compliance needs.',
      price: 'Custom', period: 'contact sales', cta: 'Contact Sales', ctaLink: '/auth/register',
      features: ['Everything in Professional', 'SSO / SAML', 'Dedicated instance', 'Custom roles', 'SLA & audit', 'Unlimited storage'],
    },
  ];

  testimonials: Testimonial[] = [
    { name: 'Sarah Mitchell', role: 'COO, Northwind Group', quote: 'BusinessOS replaced five tools. Our ops cycle is faster and leadership finally has one source of truth.', initials: 'SM', color: '#2563EB' },
    { name: 'David Okafor', role: 'CEO, Brightlogistics', quote: 'The workflow engine alone paid for the platform. Approvals that took days now finish in hours.', initials: 'DO', color: '#14B8A6' },
    { name: 'Priya Nair', role: 'Head of HR, Vertex Labs', quote: 'HR, payroll and onboarding in one place. Our employees love the self-service portal.', initials: 'PN', color: '#8B5CF6' },
    { name: 'Marco Rossi', role: 'CFO, Helios Manufacturing', quote: 'Finance visibility improved dramatically. Invoices and cash flow are finally predictable.', initials: 'MR', color: '#F59E0B' },
    { name: 'Emily Chen', role: 'IT Director, Cloudframe', quote: 'Tenant isolation and audit logs made compliance reviews painless. Enterprise-ready out of the box.', initials: 'EC', color: '#EF4444' },
    { name: 'James Carter', role: 'Founder, Stride Studio', quote: 'We onboarded in a day. The AI assistant drafts client emails better than I do.', initials: 'JC', color: '#0EA5E9' },
  ];

  faqs: { question: string; answer: string; }[] = [
    { question: 'How long does it take to get started?', answer: 'Most companies are live within a day. Sign up, create your workspace and invite your team — no installation required.' },
    { question: 'Is my company data isolated from others?', answer: 'Yes. BusinessOS is multi-tenant with hard data isolation per company, backed by encryption and strict access controls.' },
    { question: 'Can I automate approval workflows?', answer: 'Absolutely. The workflow engine lets you model request → approval → task → review → invoice → completed processes with full tracking.' },
    { question: 'Does BusinessOS include AI?', answer: 'Yes. An embedded AI assistant helps with summaries, document analysis, email generation, reports and smart suggestions.' },
    { question: 'What about clients and service requests?', answer: 'Clients can self-register and submit service requests through a branded portal, then track progress like parcel tracking.' },
    { question: 'Is there an enterprise plan?', answer: 'Yes. Enterprise includes SSO/SAML, dedicated instances, custom roles, SLA, audit and unlimited storage. Contact sales for pricing.' },
  ];

  trusted = ['Google', 'Microsoft', 'Oracle', 'Stripe', 'OpenAI', 'Amazon', 'IBM', 'SAP'];

  workflowSteps = ['Request', 'Approval', 'Task', 'Review', 'Invoice', 'Completed'];

  setTab(id: string): void { this.activeTab = id; }
}

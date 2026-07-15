import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-pricing',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './pricing.html',
  styleUrls: ['./pricing.scss']
})
export class PricingComponent {
  trustBadges = ['No setup fees', 'Cancel anytime', 'Secure & reliable', '24/7 Support'];

  plans = signal([
    {
      name: 'Free Trial',
      price: '$0',
      period: '14 days free',
      features: ['Full platform access', 'No credit card required', 'Explore all core features'],
      highlighted: false,
      btnText: 'Start Free Trial',
      btnClass: 'btn-outline-purple'
    },
    {
      name: 'Starter',
      price: '$20',
      period: 'per user/month',
      features: ['Up to 10 team members', 'Core CRM & HRM modules', 'Standard reporting', 'Email support'],
      highlighted: false,
      btnText: 'Get Started',
      btnClass: 'btn-outline-purple'
    },
    {
      name: 'Professional',
      price: '$100',
      period: 'per user/month',
      features: ['Unlimited team members', 'All modules included', 'AI-powered automation', 'Advanced analytics', 'Priority support'],
      highlighted: true,
      btnText: 'Get Started',
      btnClass: 'btn-purple-solid'
    },
    {
      name: 'Enterprise',
      price: '$200',
      period: 'per user/month',
      features: ['Custom integrations', 'Dedicated account manager', 'SLA & premium support', 'Advanced security', 'Onboarding & training'],
      highlighted: false,
      btnText: 'Contact Sales',
      btnClass: 'btn-outline-purple'
    }
  ]);
}

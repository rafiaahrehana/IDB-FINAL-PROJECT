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
  plans = signal([
    {
      name: 'Starter',
      price: '$49',
      period: 'per user/month',
      desc: 'Essential features for small teams.',
      features: ['CRM & Basic HRM', 'Standard Support', '5GB Storage', 'Standard Reports'],
      highlighted: false,
      btnClass: 'btn-outline-bos'
    },
    {
      name: 'Professional',
      price: '$99',
      period: 'per user/month',
      desc: 'Advanced tools for growing companies.',
      features: ['All Starter Features', 'Full Suite Access', 'AI Assistant', 'Advanced Workflows', '24/7 Priority Support'],
      highlighted: true,
      btnClass: 'btn-primary-bos'
    },
    {
      name: 'Enterprise',
      price: 'Custom',
      period: 'billed annually',
      desc: 'Dedicated resources and custom SLA.',
      features: ['All Pro Features', 'Dedicated Success Manager', 'Custom Integrations', 'On-Premise Option', 'Volume Discounts'],
      highlighted: false,
      btnClass: 'btn-outline-bos'
    }
  ]);
}

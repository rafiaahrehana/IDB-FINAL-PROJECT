import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-why-business-os',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './why-business-os.html',
  styleUrls: ['./why-business-os.scss']
})
export class WhyBusinessOsComponent {
  features = signal([
    {
      title: 'Multi-Tenant SaaS',
      icon: 'bi-building',
      desc: 'Isolate data across organizations seamlessly with military-grade containerization.',
      featured: false,
      borderColor: 'rgba(59, 130, 246, 0.35)',
      hoverBorderColor: 'rgba(59, 130, 246, 0.8)',
      iconBg: 'rgba(59, 130, 246, 0.1)',
      iconColor: '#2563eb'
    },
    {
      title: 'AI Powered',
      icon: 'bi-robot',
      desc: 'Automate workflows with intelligent AI agents that plan, execute, and continuously learn across your entire operational surface.',
      featured: true,
      borderColor: 'rgba(167, 139, 250, 0.55)',
      hoverBorderColor: 'rgba(167, 139, 250, 1)',
      iconBg: 'rgba(255, 255, 255, 0.18)',
      iconColor: '#ffffff'
    },
    {
      title: 'Enterprise Security',
      icon: 'bi-shield-check',
      desc: 'Bank-grade end-to-end encryption, automatic key rotation, and role-based access controls.',
      featured: false,
      borderColor: 'rgba(20, 184, 166, 0.35)',
      hoverBorderColor: 'rgba(20, 184, 166, 0.8)',
      iconBg: 'rgba(20, 184, 166, 0.1)',
      iconColor: '#0d9488'
    },
    {
      title: 'Workflow Automation',
      icon: 'bi-diagram-3',
      desc: 'Drag-and-drop visual builders engineered to map complex business logics effortlessly.',
      featured: false,
      borderColor: 'rgba(124, 58, 237, 0.35)',
      hoverBorderColor: 'rgba(124, 58, 237, 0.8)',
      iconBg: 'rgba(124, 58, 237, 0.1)',
      iconColor: '#7c3aed'
    },
    {
      title: 'Cloud Native',
      icon: 'bi-cloud',
      desc: 'Deploy anywhere securely with standard Docker and Kubernetes ready architectures.',
      featured: false,
      borderColor: 'rgba(6, 182, 212, 0.35)',
      hoverBorderColor: 'rgba(6, 182, 212, 0.8)',
      iconBg: 'rgba(6, 182, 212, 0.1)',
      iconColor: '#0891b2'
    },
    {
      title: 'Scalable Architecture',
      icon: 'bi-graph-up-arrow',
      desc: 'Grow your business without performance hits. Scale resource instances horizontally on-demand in real-time.',
      featured: false,
      cta: 'Explore Architecture',
      borderColor: 'rgba(245, 158, 11, 0.35)',
      hoverBorderColor: 'rgba(245, 158, 11, 0.8)',
      iconBg: 'rgba(245, 158, 11, 0.1)',
      iconColor: '#d97706'
    }
  ]);
}

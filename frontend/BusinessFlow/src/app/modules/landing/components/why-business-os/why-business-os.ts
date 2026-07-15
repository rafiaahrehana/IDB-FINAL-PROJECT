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
    { title: 'Multi-Tenant SaaS', icon: 'bi-building', desc: 'Isolate data across organizations seamlessly with military-grade containerization.', featured: false },
    { title: 'AI Powered', icon: 'bi-robot', desc: 'Automate workflows with intelligent AI agents that plan, execute, and continuously learn across your entire operational surface.', featured: true },
    { title: 'Enterprise Security', icon: 'bi-shield-check', desc: 'Bank-grade end-to-end encryption, automatic key rotation, and role-based access controls.', featured: false },
    { title: 'Workflow Automation', icon: 'bi-diagram-3', desc: 'Drag-and-drop visual builders engineered to map complex business logics effortlessly.', featured: false },
    { title: 'Cloud Native', icon: 'bi-cloud', desc: 'Deploy anywhere securely with standard Docker and Kubernetes ready architectures.', featured: false },
    { title: 'Scalable Architecture', icon: 'bi-graph-up-arrow', desc: 'Grow your business without performance hits. Scale resource instances horizontally on-demand in real-time.', featured: false, cta: 'Explore Architecture' }
  ]);
}

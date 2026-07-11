import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-why-business-os',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './why-business-os.html',
  styleUrls: ['./why-business-os.scss']
})
export class WhyBusinessOsComponent {
  features = signal([
    { title: 'Multi-Tenant SaaS', icon: 'bi-building', desc: 'Isolate data across organizations seamlessly.', featured: false },
    { title: 'AI Powered', icon: 'bi-robot', desc: 'Automate tasks with intelligent AI agents that plan, execute and learn across your entire workflow.', featured: true },
    { title: 'Enterprise Security', icon: 'bi-shield-lock', desc: 'Bank-grade encryption and role-based access.', featured: false },
    { title: 'Workflow Automation', icon: 'bi-diagram-3', desc: 'Visual builders for complex business logic.', featured: false },
    { title: 'Cloud Native', icon: 'bi-cloud', desc: 'Deploy anywhere with Kubernetes ready architecture.', featured: false },
    { title: 'Scalable Architecture', icon: 'bi-graph-up-arrow', desc: 'Grows with your business without performance hits.', featured: false }
  ]);
}
